package kz.kaspi.travel.core.search.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightOffer {
    private String id;
    private String airline;
    private String departureCity;
    private String arrivalCity;
    private LocalDateTime departureTime;
    private BigDecimal price;
}
