package kz.kaspi.travel.loyalty.service;

import kz.kaspi.travel.shared.command.ReserveBonusesCommand;
import kz.kaspi.travel.shared.event.BonusesReservedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoyaltyCommandHandler {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    // Слушаем команды от Оркестратора
    @KafkaListener(topics = "loyalty-commands", groupId = "loyalty-group")
    public void handleReserveBonuses(ReserveBonusesCommand command) {
        System.out.println("[LOYALTY SERVICE] Получена команда заморозить " + command.getAmount()
                + " бонусов для заказа: " + command.getBookingId());

        System.out.println("[LOYALTY SERVICE] Бонусы успешно заморожены. Отвечаем Оркестратору...");

        BonusesReservedEvent event = BonusesReservedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .bookingId(command.getBookingId())
                .userId(command.getUserId())
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send("loyalty-events", event.getBookingId(), event);
    }
}
