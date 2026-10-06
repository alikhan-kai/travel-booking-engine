package kz.kaspi.travel.core.booking.messaging;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookingCreatedEvent {
    private Long bookingId;
    private String flightId;
    private String seatNumber;
    private LocalDateTime timestamp;
}
