package kz.kaspi.travel.core.booking.api;

import org.springframework.web.bind.annotation.*;
import kz.kaspi.travel.core.booking.model.Booking;
import kz.kaspi.travel.core.booking.service.BookingSagaOrchestrator;
import kz.kaspi.travel.core.booking.service.RefundSagaOrchestrator;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingSagaOrchestrator sagaOrchestrator;
    private final RefundSagaOrchestrator refundSagaOrchestrator;

    @PostMapping("/hold")
    public Mono<Booking> holdSeat(
            @RequestParam("flightId") String flightId,
            @RequestParam("seatNumber") String seatNumber,
            @RequestParam("iin") String iin) {

        Booking booking = new Booking();
        booking.setFlightId(flightId);
        booking.setSeatNumber(seatNumber);

        // Запускаем нашу Сагу вместе с ИИН пассажира!
        return sagaOrchestrator.startSaga(booking, iin);
    }

    @PostMapping("/{id}/refund")
    public Mono<Booking> refundBooking(@PathVariable("id") Long id) {
        return refundSagaOrchestrator.processRefund(id);
    }
}
