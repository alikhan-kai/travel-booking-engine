package kz.kaspi.travel.shared.event;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentProcessedEvent implements Event {
    private String eventId;
    private String bookingId;
    private String status; // "SUCCESS" или "INSUFFICIENT_FUNDS"
    private String transactionId;
    private LocalDateTime timestamp;
}
