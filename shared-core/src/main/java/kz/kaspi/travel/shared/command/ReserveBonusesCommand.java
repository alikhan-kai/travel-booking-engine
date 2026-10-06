package kz.kaspi.travel.shared.command;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReserveBonusesCommand implements Command {
    private String bookingId;   // Для какого заказа
    private String userId;      // Чьи бонусы
    private Integer amount;     // Сколько бонусов заморозить
}
