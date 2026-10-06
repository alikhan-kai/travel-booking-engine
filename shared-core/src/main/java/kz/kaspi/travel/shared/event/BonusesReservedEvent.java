package kz.kaspi.travel.shared.event;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class BonusesReservedEvent implements Event {
    private String eventId;
    private String bookingId;
    private String userId;
    private LocalDateTime timestamp;
}
