package kz.kaspi.travel.core.booking.service;

import kz.kaspi.travel.core.booking.model.Booking;
import kz.kaspi.travel.core.booking.repository.BookingRepository;
import kz.kaspi.travel.shared.command.ReserveBonusesCommand;
import kz.kaspi.travel.shared.event.BonusesReservedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class BookingSagaOrchestrator {

    private final BookingRepository bookingRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public Mono<Booking> startSaga(Booking booking) {
        booking.setStatus("NEW");

        return bookingRepository.save(booking)
                .doOnNext(saved -> {
                    ReserveBonusesCommand command = ReserveBonusesCommand.builder()
                            .bookingId(saved.getId().toString())
                            .userId("USER_123")
                            .amount(5000)
                            .build();

                    kafkaTemplate.send("loyalty-commands", saved.getId().toString(), command);
                    System.out.println("🚀 [SAGA START] Отправлена команда на заморозку бонусов.");
                });
    }

    @KafkaListener(topics = "loyalty-events", groupId = "orchestrator-group")
    public void onBonusesReserved(BonusesReservedEvent event) {
        System.out.println("✅ [SAGA СТАТУС] Бонусы заморожены для заказа: " + event.getBookingId());

        bookingRepository.findById(Long.valueOf(event.getBookingId()))
                .flatMap(booking -> {
                    booking.setStatus("BONUSES_FROZEN");
                    return bookingRepository.save(booking);
                })
                .doOnNext(saved -> {
                    System.out.println(
                            "🚀 [SAGA ПРОДОЛЖЕНИЕ] Отправляем команду в Payment Gateway на списание денег с карты Kaspi Gold!");

                    // TODO: kafkaTemplate.send("payment-commands", new ChargeCardCommand(...));
                })
                .subscribe();
    }
}