package kz.kaspi.travel.core.booking.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import kz.kaspi.travel.core.booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentEventConsumer {
    private final BookingRepository bookingRepository;

    // Заставляем спринг вечно слушать топик кафки
    @KafkaListener(topics = "payment-success-topic", groupId = "travel-core-group")
    public void consumePaymentEvent(PaymentSuccessEvent event) {
        System.out.println("Payment Success Received for bookingId: " + event.getBookingId());

        // Находим заказ в базе -> Меняем статус -> Сохраняем обратно
        bookingRepository.findById(event.getBookingId())
                .flatMap(booking -> {
                    booking.setStatus("CONFIRMED");
                    return bookingRepository.save(booking);
                })
                // Подписываем, вызов subscribe()
                .subscribe(
                        saved -> System.out.println("Ticket to order" + saved.getId() + "successfully discharged"),
                        error -> System.err.println("Error" + error.getMessage()));
    }
}
