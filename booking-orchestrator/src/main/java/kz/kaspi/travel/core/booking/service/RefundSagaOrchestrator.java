package kz.kaspi.travel.core.booking.service;

import java.math.BigDecimal;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import kz.kaspi.travel.core.booking.model.Booking;
import kz.kaspi.travel.core.booking.repository.BookingRepository;
import kz.kaspi.travel.shared.command.RefundCardCommand;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RefundSagaOrchestrator {

    private final BookingRepository bookingRepository;
    private final FareRuleEngine fareRuleEngine;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public Mono<Booking> processRefund(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .flatMap(booking -> {
                    BigDecimal ticketPrice = new BigDecimal("100000");
                    BigDecimal amountToRefund = fareRuleEngine.calculateRefundAmount(ticketPrice,
                            booking.getFareType());
                    System.out.println("[REFUND SAGA] Processing Refund for booking " + booking.getFareType()
                            + " Refund amount: " + amountToRefund + "tg.");
                    booking.setStatus("CANCELED_REFUND_IN_PROGRESS");

                    return bookingRepository.save(booking)
                            .doOnNext(saved -> {
                                RefundCardCommand command = RefundCardCommand.builder()
                                        .bookingId(saved.getId().toString())
                                        .userId("USER_123")
                                        .amountToRefund(amountToRefund)
                                        .build();

                                kafkaTemplate.send("payment-commands", saved.getId().toString(), command);
                                System.out.println("[REFUND SAGA] The return command has been sent to Kafka!");
                            });
                });
    }
}
