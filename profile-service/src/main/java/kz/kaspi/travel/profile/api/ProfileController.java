package kz.kaspi.travel.profile.api;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.util.Set;

@RestController
@RequestMapping("/v1/profiles")
public class ProfileController {
    private final Set<String> blacklistedIins = Set.of(
            "900101400001",
            "850505300002");

    @GetMapping("validate")
    public Mono<ValidationResponse> validatePassenger(@RequestParam String iin) {
        System.out.println("[PROFILE SERVICE] Check IIN " + iin + " in db for blacklist...");

        if (blacklistedIins.contains(iin)) {
            System.out.println("[PROFILE SERVICE] IIN " + iin + " is BLACKLISTED");
            return Mono.just(new ValidationResponse(false,
                    "The user is listed in the debtors register. Travel abroad is prohibited."));
        }
        System.out.println("[-PROFILE SERVICE] IIN " + iin + " is CLEAN");
        return Mono.just(new ValidationResponse(true, "OK"));
    }

    public record ValidationResponse(boolean isValid, String message) {
    }
}
