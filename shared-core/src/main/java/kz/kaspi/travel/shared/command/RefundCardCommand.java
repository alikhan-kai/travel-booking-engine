package kz.kaspi.travel.shared.command;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class RefundCardCommand {
    private String bookingId;
    private String userId;
    private BigDecimal amountToRefund;
}
