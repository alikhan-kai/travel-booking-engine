CREATE TABLE IF NOT EXISTS bookings(
    id SERIAL PRIMARY KEY,
    flight_id  VARCHAR(50) NOT NULL,
    seat_number VARCHAR(10) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL
);