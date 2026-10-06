package kz.kaspi.travel.core.booking.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kz.kaspi.travel.core.booking.model.Booking;
import kz.kaspi.travel.core.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/hold")
    public Mono<Booking> holdSeat(
            @RequestParam String flightId,
            @RequestParam String seatNumber) {

        return bookingService.holdSeat(flightId, seatNumber);
    }
}
