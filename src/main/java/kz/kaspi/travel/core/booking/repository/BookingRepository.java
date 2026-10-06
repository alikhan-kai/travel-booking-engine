package kz.kaspi.travel.core.booking.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import kz.kaspi.travel.core.booking.model.Booking;

public interface BookingRepository extends ReactiveCrudRepository<Booking, Long> {
    // ReactiveCrudRepository уже содержит под капотом методы save(), findById() и
    // умеет неблокирующе общаться с PostgreSQL
}
