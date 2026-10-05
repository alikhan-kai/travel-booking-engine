package kz.kaspi.travel.core.search.client;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Component;

import kz.kaspi.travel.core.search.model.FlightOffer;
import reactor.core.publisher.Flux;

@Component
public class AirAstanaClient implements FlightProviderClient {

    @Override
    public Flux<FlightOffer> search(String departure, String arrival) {
        FlightOffer offer = FlightOffer.builder()
                .id(UUID.randomUUID().toString())
                .airline("Air Astana")
                .departureCity(departure)
                .arrivalCity(arrival)
                .departureTime(LocalDateTime.now().plusDays(2))
                .price(new BigDecimal("45000"))
                .build();
        return Flux.just(offer).delayElements(Duration.ofMillis(300));
    }
}
