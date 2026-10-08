package kz.kaspi.travel.shared.command;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RefundCardCommand {
    private String bookingId;
    private String userId;
    private BigDecimal amountToRefund;
}
