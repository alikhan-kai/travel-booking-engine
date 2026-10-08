package kz.kaspi.travel.core.booking.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Builder;
import lombok.Data;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("bookings")
public class Booking {

    @Id
    private Long id;

    private String flightId;
    private String seatNumber;

    private String status;
    private LocalDateTime createdAt;

    private String fareType;
}
