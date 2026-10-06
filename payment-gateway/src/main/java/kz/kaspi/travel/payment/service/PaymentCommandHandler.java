package kz.kaspi.travel.payment.service;

import kz.kaspi.travel.shared.command.ChargeCardCommand;
import kz.kaspi.travel.shared.event.PaymentProcessedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentCommandHandler {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "payment-commands", groupId = "payment-group")
    public void handlePayment(ChargeCardCommand command) {
        System.out.println("[PAYMENT GATEWAY] Запрос на списание " + command.getAmount() + " тенге. Способ: "
                + command.getPaymentType());

        System.out.println("[PAYMENT GATEWAY] Деньги успешно списаны!");

        PaymentProcessedEvent event = PaymentProcessedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .bookingId(command.getBookingId())
                .status("SUCCESS")
                .transactionId("TRX-" + UUID.randomUUID().toString().substring(0, 8))
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send("payment-events", event.getBookingId(), event);
    }
}