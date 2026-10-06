package kz.kaspi.travel.shared.event;

import java.time.LocalDateTime;

// Базовый интерфейс для всех событий в системе
public interface Event {
    String getEventId(); // Уникальный ID самого события (для защиты от дублей)
    LocalDateTime getTimestamp(); // Когда это случилось
}
