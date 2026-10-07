package kz.kaspi.travel.core.search.service;

import kz.kaspi.travel.core.search.client.FlightProviderClient;
import kz.kaspi.travel.core.search.model.FlightOffer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FlightSearchService {

    private final List<FlightProviderClient> providers;

    // WebClient для асинхронных HTTP запросов к другим микросервисам
    private final WebClient webClient = WebClient.create("http://localhost:8085");

    public Flux<FlightOffer> searchFlights(String departure, String arrival) {
        // 1. Собираем все потоки
        List<Flux<FlightOffer>> providerFluxes = new java.util.ArrayList<>(providers.stream()
                .map(provider -> provider.search(departure, arrival))
                .toList());

        // Добавляем тестовые рейсы с пересадкой в Астане (NQZ), чтобы Routing Engine было что искать!
        providerFluxes.add(Flux.just(
            FlightOffer.builder().id("1").airline("SCAT").departureCity(departure).arrivalCity("NQZ").departureTime(java.time.LocalDateTime.now().plusDays(1)).price(new java.math.BigDecimal("15000")).build(),
            FlightOffer.builder().id("2").airline("FlyArystan").departureCity("NQZ").arrivalCity(arrival).departureTime(java.time.LocalDateTime.now().plusDays(1).plusHours(5)).price(new java.math.BigDecimal("20000")).build()
        ));

        // 2. Объединяем их ответы в один список
        Flux<FlightOffer> directFlights = Flux.fromIterable(providerFluxes)
                .flatMapDelayError(flux -> flux, 32, 32)
                .timeout(Duration.ofMillis(2500))
                .onErrorResume(throwable -> Flux.empty());

        // 3. Отправляем все найденные прямые рейсы в Routing Engine для поиска стыковок
        return directFlights.collectList().flatMapMany(flightsList -> {

            // Если ничего не нашли, возвращаем пустоту
            if (flightsList.isEmpty()) {
                return Flux.empty();
            }

            // Делаем POST запрос в микросервис Routing Engine
            Flux<FlightOffer> smartRoutes = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/routing/calculate")
                            .queryParam("origin", departure)
                            .queryParam("destination", arrival)
                            .build())
                    .bodyValue(flightsList) // Отправляем список рейсов в теле запроса
                    .retrieve()
                    .bodyToFlux(FlightOffer.class)
                    .onErrorResume(e -> {
                        System.err.println("⚠️ Routing Engine недоступен: " + e.getMessage());
                        return Flux.empty(); // Если движок упал, просто не показываем стыковки
                    });

            // 4. Склеиваем оригинальные (прямые) рейсы и умные стыковки!
            return Flux.fromIterable(flightsList).mergeWith(smartRoutes);
        });
    }
}