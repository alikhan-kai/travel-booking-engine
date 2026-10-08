package kz.kaspi.travel.core.booking.service;

import kz.kaspi.travel.core.booking.model.Booking;
import kz.kaspi.travel.core.booking.repository.BookingRepository;
import kz.kaspi.travel.shared.command.ChargeCardCommand;
import kz.kaspi.travel.shared.command.ReserveBonusesCommand;
import kz.kaspi.travel.shared.event.BonusesReservedEvent;
import kz.kaspi.travel.shared.event.PaymentProcessedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class BookingSagaOrchestrator {

    private final BookingRepository bookingRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final org.springframework.web.reactive.function.client.WebClient.Builder webClientBuilder;

    public record ValidationResponse(boolean isValid, String message) {
    }

    public Mono<Booking> startSaga(Booking booking, String passengerIin) {

        System.out.println("[SAGA PRE-CHECK] Отправляем ИИН " + passengerIin + " на проверку в Profile Service...");

        org.springframework.web.reactive.function.client.WebClient profileClient = 
                webClientBuilder.baseUrl("http://localhost:8086").build();

        return profileClient.get()
                .uri("/v1/profiles/validate?iin=" + passengerIin)
                .retrieve()
                .bodyToMono(ValidationResponse.class)
                .flatMap(validation -> {
                    if (!validation.isValid()) {
                        System.out.println("[SAGA ОТМЕНА] Бронь отклонена: " + validation.message());
                        booking.setStatus("REJECTED_BY_ANTIFRAUD");
                        return bookingRepository.save(booking); // Сохраняем в БД как отклоненный
                    }

                    System.out.println("[SAGA PRE-CHECK] Пассажир чист! Начинаем транзакцию.");
                    booking.setStatus("NEW");

                    return bookingRepository.save(booking)
                            .doOnNext(saved -> {
                                ReserveBonusesCommand command = ReserveBonusesCommand.builder()
                                        .bookingId(saved.getId().toString())
                                        .userId(passengerIin)
                                        .amount(5000)
                                        .build();

                                kafkaTemplate.send("loyalty-commands", saved.getId().toString(), command);
                                System.out.println("[SAGA START] Отправлена команда на заморозку бонусов.");
                            });
                });
    }

    @KafkaListener(topics = "loyalty-events", groupId = "orchestrator-group")
    public void onBonusesReserved(BonusesReservedEvent event) {
        System.out.println("[SAGA СТАТУС] Бонусы заморожены для заказа: " + event.getBookingId());

        bookingRepository.findById(Long.valueOf(event.getBookingId()))
                .flatMap(booking -> {
                    booking.setStatus("BONUSES_FROZEN");
                    return bookingRepository.save(booking);
                })
                .doOnNext(saved -> {
                    System.out.println(
                            "[SAGA ПРОДОЛЖЕНИЕ] Отправляем команду в Payment Gateway на списание денег!");

                    ChargeCardCommand chargeCardCommand = ChargeCardCommand.builder()
                            .bookingId(saved.getId().toString())
                            .userId(event.getUserId())
                            .amount(new java.math.BigDecimal("35000"))
                            .paymentType("KASPI_RED")
                            .build();
                    kafkaTemplate.send("payment-commands", saved.getId().toString(), chargeCardCommand);
                })
                .subscribe();
    }

    @KafkaListener(topics = "payment-events", groupId = "orchestrator-group")
    public void onPaymentProcessed(PaymentProcessedEvent event) {
        if ("SUCCESS".equals(event.getStatus())) {
            System.out.println("[SAGA ФИНАЛ] Оплата прошла! Выписываем билет для заказа: " + event.getBookingId());

            bookingRepository.findById(Long.valueOf(event.getBookingId()))
                    .flatMap(booking -> {
                        booking.setStatus("TICKET_ISSUED");
                        return bookingRepository.save(booking);
                    })
                    .subscribe();
        } else {
            System.out.println("[SAGA ОТМЕНА] Ошибка оплаты. Запускаем Компенсационную Транзакцию (Возврат бонусов)!");
        }
    }
}