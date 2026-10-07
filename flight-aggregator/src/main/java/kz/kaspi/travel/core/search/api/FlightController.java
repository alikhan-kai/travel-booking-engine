package kz.kaspi.travel.core.search.api;

import kz.kaspi.travel.core.search.model.FlightOffer;
import kz.kaspi.travel.core.search.service.FlightSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/v1/flights")
@RequiredArgsConstructor
public class FlightController {

    private final FlightSearchService flightSearchService;

    @GetMapping("/search")
    public Flux<FlightOffer> search(@RequestParam String departure, @RequestParam String arrival) {
        return flightSearchService.searchFlights(departure, arrival);
    }
}
