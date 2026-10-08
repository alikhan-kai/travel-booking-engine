package kz.kaspi.travel.core.search.service;

import kz.kaspi.travel.core.search.client.FlightProviderClient;
import kz.kaspi.travel.core.search.model.FlightOffer;
import lombok.RequiredArgsConstructor;

import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FlightSearchService {

    private final List<FlightProviderClient> providers;

    private final CacheManager cacheManager;

    // WebClient для асинхронных HTTP запросов к другим микросервисам
    private final WebClient webClient = WebClient.create("http://localhost:8085");

    public Flux<FlightOffer> searchFlights(String departure, String arrival) {
        // Уникальный ключ маршрута, например: "ALA-KGF"
        String cacheKey = departure + "-" + arrival;
        org.springframework.cache.Cache cache = cacheManager.getCache("flights");

        // 1.Проверка кэша: Если данные уже есть в памяти — отдаем их моментально!
        if (cache != null && cache.get(cacheKey) != null) {
            System.out.println("1.[HIGHLOAD] Taking from cache (0 ms): " + cacheKey);
            List<FlightOffer> cachedFlights = (List<FlightOffer>) cache.get(cacheKey).get();
            return Flux.fromIterable(cachedFlights);
        }

        System.out.println("1.[HIGHLOAD] Cache empty. Performing long search through API...");

        List<Flux<FlightOffer>> providerFluxes = new java.util.ArrayList<>(providers.stream()
                .map(provider -> provider.search(departure, arrival)).toList());

        providerFluxes.add(Flux.just(
                FlightOffer.builder().id("1").airline("SCAT").departureCity(departure).arrivalCity("NQZ")
                        .departureTime(java.time.LocalDateTime.now().plusDays(1))
                        .price(new java.math.BigDecimal("15000")).build(),
                FlightOffer.builder().id("2").airline("FlyArystan").departureCity("NQZ").arrivalCity(arrival)
                        .departureTime(java.time.LocalDateTime.now().plusDays(1).plusHours(5))
                        .price(new java.math.BigDecimal("20000")).build()));

        Flux<FlightOffer> directFlights = Flux.fromIterable(providerFluxes)
                .flatMapDelayError(flux -> flux, 32, 32)
                .timeout(Duration.ofMillis(2500))
                .onErrorResume(throwable -> Flux.empty());

        return directFlights.collectList().flatMapMany(flightsList -> {
            if (flightsList.isEmpty())
                return Flux.empty();

            Flux<FlightOffer> smartRoutes = webClient.post()
                    .uri(uriBuilder -> uriBuilder.path("/v1/routing/calculate")
                            .queryParam("origin", departure)
                            .queryParam("destination", arrival).build())
                    .bodyValue(flightsList).retrieve().bodyToFlux(FlightOffer.class)
                    .onErrorResume(e -> Flux.empty());

            // 2.Сохранение в кэш: Собираем финальный результат и кладем в оперативку на 5
            // минут
            return Flux.fromIterable(flightsList).mergeWith(smartRoutes).collectList().doOnNext(finalList -> {
                if (cache != null) {
                    System.out.println("[HIGHLOAD] Saved in cache: " + cacheKey);
                    cache.put(cacheKey, finalList);
                }
            }).flatMapMany(Flux::fromIterable);
        });
    }
}