package kz.kaspi.travel.core.search.service;

import java.time.Duration;
import java.util.List;

import org.springframework.stereotype.Service;

import kz.kaspi.travel.core.search.client.FlightProviderClient;
import kz.kaspi.travel.core.search.model.FlightOffer;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class FlightSearchService {

    private final List<FlightProviderClient> providers;

    public Flux<FlightOffer> searchFlights(String departure, String arrival) {

        // 1.Берем наш List<FlightProviderClient> и превращаем его в Flux
        return Flux.fromIterable(providers)

                // 2.У каждого провайдера вызываем поиск (возвращается Flux внутри Flux)
                .map(provider -> provider.search(departure, arrival))

                // 3.Безопасно делаем все потоки в один.
                .flatMapDelayError(flux -> flux, 32, 32)

                // 4.Отсекаем медленных
                .timeout(Duration.ofMillis(2500))

                // 5.Защита от прерывания потока
                .onErrorResume(throwable -> Flux.empty());
    }
}
