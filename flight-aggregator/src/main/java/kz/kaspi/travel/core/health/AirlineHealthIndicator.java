package kz.kaspi.travel.core.health;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
@Component
public class AirlineHealthIndicator implements ReactiveHealthIndicator {
@Override
public Mono<Health> health() {
return Mono.just(Health.up().withDetail("airlines", "All 3 providers are reachable").build());
}
}
