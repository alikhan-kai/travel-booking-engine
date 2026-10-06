package kz.kaspi.travel.core.booking.messaging;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PaymentSuccessEvent {
    private Long bookingId;
    private String transactionId;
    private LocalDateTime transactionTime;
}
