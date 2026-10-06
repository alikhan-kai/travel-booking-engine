package kz.kaspi.travel.core.booking.service;

import kz.kaspi.travel.core.booking.model.Booking;
import kz.kaspi.travel.core.booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final BookingRepository bookingRepository;

    public Mono<Booking> holdSeat(String flightId, String seatNumber) {
        // Уникальный ключ для кресла (например: flight:KC123:seat:14A)
        String redisKey = "flight: " + flightId + ":seat:" + seatNumber;

        // 1.Атомарная блокировка в Redis (SETNX - Set if Not eXists) с таймером на 15
        // минут
        return redisTemplate.opsForValue()
                .setIfAbsent(redisKey, "LOCKED", Duration.ofMinutes(15))
                .flatMap(isLocked -> {
                    if (Boolean.TRUE.equals(isLocked)) {
                        Booking newBooking = Booking.builder()
                                .flightId(flightId)
                                .seatNumber(seatNumber)
                                .status("PENDING")
                                .createdAt(LocalDateTime.now())
                                .build();
                        return bookingRepository.save(newBooking);
                    } else {
                        return Mono.error(new RuntimeException("Seat is already booked:" + seatNumber));
                    }
                });

    }
}
