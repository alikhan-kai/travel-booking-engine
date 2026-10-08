package kz.kaspi.travel.core.booking.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

@Service
public class FareRuleEngine {

    public BigDecimal calculateRefaundAmount(BigDecimal ticketPrice, String fareType) {
        return switch (fareType.toUpperCase()) {
            case "BASIC" -> BigDecimal.ZERO;
            case "STANDARD" -> ticketPrice.subtract(new BigDecimal("15000"));
            case "FLEX" -> ticketPrice;
            default -> throw new IllegalArgumentException("Unknown fare type:" + fareType);
        };
    }
}
