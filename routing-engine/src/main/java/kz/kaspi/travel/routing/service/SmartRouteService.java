package kz.kaspi.travel.routing.service;

import org.springframework.stereotype.Service;
import lombok.Data;
import lombok.Builder;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class SmartRouteService {

    @Data
    @Builder
    public static class FlightSegment {
        private String airline;
        private String departureCity;
        private String arrivalCity;
        private LocalDateTime departureTime;
        private LocalDateTime arrivalTime;
    }

    public List<FlightSegment> calculateSmartRoutes(List<FlightSegment> allFlights, String origin, String destination) {
        List<FlightSegment> smartRoutes = new ArrayList<>();

        for (FlightSegment firstLeg : allFlights) {
            if (firstLeg.getDepartureCity().equals(origin) && !firstLeg.getArrivalCity().equals(destination)) {

                for (FlightSegment secondLeg : allFlights) {
                    if (secondLeg.getDepartureCity().equals(firstLeg.getArrivalCity())
                            && secondLeg.getArrivalCity().equals(destination)) {

                        // РџРѕСЃРєРѕР»СЊРєСѓ mock-РєР»РёРµРЅС‚С‹ РЅРµ РїСЂРёСЃС‹Р»Р°СЋС‚ arrivalTime (РѕРЅ null), СЌРјСѓР»РёСЂСѓРµРј РїРѕР»РµС‚ РІ 2 С‡Р°СЃР°
                        Duration layover = Duration.between(firstLeg.getDepartureTime().plusHours(2), secondLeg.getDepartureTime());

                        if (layover.toHours() >= 2 && layover.toHours() <= 12) {
                            System.out.println("рџ”— [ROUTING] РќР°Р№РґРµРЅР° СѓРјРЅР°СЏ СЃС‚С‹РєРѕРІРєР°! " +
                                    firstLeg.getAirline() + " + " + secondLeg.getAirline() +
                                    " С‡РµСЂРµР· " + firstLeg.getArrivalCity());

                            smartRoutes.add(FlightSegment.builder()
                                    .airline("KASPI_SMART: " + firstLeg.getAirline() + "+" + secondLeg.getAirline())
                                    .departureCity(firstLeg.getDepartureCity())
                                    .arrivalCity(secondLeg.getArrivalCity())
                                    .departureTime(firstLeg.getDepartureTime())
                                    .arrivalTime(secondLeg.getArrivalTime())
                                    .build());
                        }
                    }
                }
            }
        }
        return smartRoutes;
    }
}
