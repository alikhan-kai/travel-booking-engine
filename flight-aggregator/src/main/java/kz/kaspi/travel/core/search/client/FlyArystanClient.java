package kz.kaspi.travel.core.search.client;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import kz.kaspi.travel.core.search.model.FlightOffer;
import reactor.core.publisher.Flux;

public class FlyArystanClient implements FlightProviderClient {
    @Override
    @CircuitBreaker(name = "flightProvider", fallbackMethod = "emptyFallback")
    public Flux<FlightOffer> search(String departure, String arrival) {
        FlightOffer offer = FlightOffer.builder()
                .id(UUID.randomUUID().toString())
                .airline("FlyArystan")
                .departureCity(departure)
                .arrivalCity(arrival)
                .departureTime(LocalDateTime.now().plusDays(2))
                .price(new BigDecimal("27000"))
                .build();
        return Flux.just(offer).delayElements(Duration.ofMillis(1400));
    }

    public Flux<FlightOffer> emptyFallback(String departure, String arrival, Throwable throwable) {
        System.err.println("Api FlyArystan currently unavailable:" + throwable.getMessage());
        return Flux.empty();
    }
}
