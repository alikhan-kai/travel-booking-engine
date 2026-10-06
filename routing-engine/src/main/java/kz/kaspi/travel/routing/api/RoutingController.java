package kz.kaspi.travel.routing.api;

import kz.kaspi.travel.routing.service.SmartRouteService;
import kz.kaspi.travel.routing.service.SmartRouteService.FlightSegment;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import java.util.List;

@RestController
@RequestMapping("/v1/routing")
@RequiredArgsConstructor
public class RoutingController {

    private final SmartRouteService smartRouteService;

    // Эндпоинт, который будет принимать JSON со всеми рейсами, и возвращать
    // стыковки
    @PostMapping("/calculate")
    public Flux<FlightSegment> calculate(@RequestParam String origin,
            @RequestParam String destination,
            @RequestBody List<FlightSegment> allFlights) {

        // Передаем пачку рейсов в наш алгоритм
        List<FlightSegment> smartRoutes = smartRouteService.calculateSmartRoutes(allFlights, origin, destination);

        // Возвращаем результат в виде реактивного потока
        return Flux.fromIterable(smartRoutes);
    }
}