package kz.kaspi.travel.core.booking.service;

import kz.kaspi.travel.core.booking.messaging.BookingCreatedEvent;
import kz.kaspi.travel.core.booking.messaging.BookingEventProducer;
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

    private final BookingEventProducer eventProducer;

    public Mono<Booking> holdSeat(String flightId, String seatNumber) {
        String redisKey = "flight:" + flightId + ":seat:" + seatNumber;

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

                        // Сохраняем в БД, затем отправляем в Kafka
                        return bookingRepository.save(newBooking)
                                // doOnNext сработает только если сохранение в БД прошло успешно
                                .doOnNext(savedBooking -> {
                                    BookingCreatedEvent event = BookingCreatedEvent.builder()
                                            .bookingId(savedBooking.getId())
                                            .flightId(savedBooking.getFlightId())
                                            .seatNumber(savedBooking.getSeatNumber())
                                            .timestamp(LocalDateTime.now())
                                            .build();

                                    eventProducer.sendBookingCreated(event);
                                });
                    } else {
                        return Mono.error(new RuntimeException("Seat already booked " + seatNumber));
                    }
                });
    }
}