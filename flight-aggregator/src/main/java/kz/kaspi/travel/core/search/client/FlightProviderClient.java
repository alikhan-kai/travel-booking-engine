package kz.kaspi.travel.core.search.client;

import kz.kaspi.travel.core.search.model.FlightOffer;
import reactor.core.publisher.Flux;

public interface FlightProviderClient {
    // Возвращает реактивный поток билетов (Flux)
    Flux<FlightOffer> search(String departure, String arrival);
}
