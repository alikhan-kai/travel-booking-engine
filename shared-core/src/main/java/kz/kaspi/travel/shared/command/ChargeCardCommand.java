package kz.kaspi.travel.shared.command;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class ChargeCardCommand implements Command {
    private String bookingId;
    private String userId;
    private BigDecimal amount; // Сумма к оплате с карты
    private String paymentType; // Например: "KASPI_GOLD" или "KASPI_RED"
}
