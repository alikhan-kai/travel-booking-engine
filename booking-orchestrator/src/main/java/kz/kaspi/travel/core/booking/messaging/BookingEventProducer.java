package kz.kaspi.travel.core.booking.messaging;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendBookingCreated(BookingCreatedEvent event) {
        kafkaTemplate.send("booking-created-topic", event.getBookingId().toString(), event);
        System.out.println("EVENT SENT: " + event);
    }
}
