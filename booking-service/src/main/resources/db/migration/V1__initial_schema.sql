-- Booking Service Database Schema

-- Создание таблицы бронирований
CREATE TABLE bookings (
    id BIGSERIAL PRIMARY KEY,
    booking_reference VARCHAR(20) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    trip_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    number_of_passengers INTEGER NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'RUB',
    special_requests TEXT,
    
    -- Payment info
    payment_id BIGINT,
    paid_amount DECIMAL(10, 2),
    paid_at TIMESTAMP,
    
    -- Cancellation info
    cancelled_at TIMESTAMP,
    cancellation_reason VARCHAR(500),
    refund_amount DECIMAL(10, 2),
    refunded_at TIMESTAMP,
    
    -- Audit
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    
    CONSTRAINT chk_passengers CHECK (number_of_passengers > 0),
    CONSTRAINT chk_total_amount CHECK (total_amount >= 0),
    CONSTRAINT chk_paid_amount CHECK (paid_amount IS NULL OR paid_amount >= 0)
);

-- Создание таблицы пассажиров
CREATE TABLE passengers (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    date_of_birth DATE NOT NULL,
    passport_number VARCHAR(50),
    passport_country VARCHAR(3),
    passport_expiry DATE,
    gender VARCHAR(10),
    email VARCHAR(100),
    phone_number VARCHAR(20),
    
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
);

-- Индексы для оптимизации
CREATE INDEX idx_bookings_user_id ON bookings(user_id);
CREATE INDEX idx_bookings_trip_id ON bookings(trip_id);
CREATE INDEX idx_bookings_status ON bookings(status);
CREATE INDEX idx_bookings_booking_reference ON bookings(booking_reference);
CREATE INDEX idx_bookings_created_at ON bookings(created_at);
CREATE INDEX idx_bookings_payment_id ON bookings(payment_id);

CREATE INDEX idx_passengers_booking_id ON passengers(booking_id);
CREATE INDEX idx_passengers_passport ON passengers(passport_number, passport_country);

-- Вставка тестовых данных
-- Booking 1: User 1 (admin) бронирует поездку в Сочи для 2 пассажиров
INSERT INTO bookings (booking_reference, user_id, trip_id, status, number_of_passengers, total_amount, created_at)
VALUES ('TM-2025-000001', 1, 1, 'CONFIRMED', 2, 90000.00, CURRENT_TIMESTAMP - INTERVAL '5 days');

INSERT INTO passengers (booking_id, first_name, last_name, date_of_birth, passport_number, passport_country, gender, email)
VALUES 
(1, 'Иван', 'Иванов', '1985-05-15', '1234567890', 'RUS', 'MALE', 'ivan@example.com'),
(1, 'Мария', 'Иванова', '1987-08-20', '0987654321', 'RUS', 'FEMALE', 'maria@example.com');

-- Booking 2: Новое бронирование в статусе PENDING
INSERT INTO bookings (booking_reference, user_id, trip_id, status, number_of_passengers, total_amount, created_at)
VALUES ('TM-2025-000002', 1, 2, 'PENDING', 1, 25000.00, CURRENT_TIMESTAMP - INTERVAL '1 day');

INSERT INTO passengers (booking_id, first_name, last_name, date_of_birth, gender)
VALUES (2, 'Петр', 'Петров', '1990-03-10', 'MALE');

-- Booking 3: Оплаченное бронирование
INSERT INTO bookings (booking_reference, user_id, trip_id, status, number_of_passengers, total_amount, payment_id, paid_amount, paid_at, created_at)
VALUES ('TM-2025-000003', 1, 3, 'PAID', 1, 89000.00, 1, 89000.00, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '3 days');

INSERT INTO passengers (booking_id, first_name, last_name, date_of_birth, passport_number, passport_country, gender, email, phone_number)
VALUES (3, 'Анна', 'Смирнова', '1992-11-25', '5555666677', 'RUS', 'FEMALE', 'anna@example.com', '+79991234567');

-- Booking 4: Отменённое бронирование
INSERT INTO bookings (booking_reference, user_id, trip_id, status, number_of_passengers, total_amount, cancelled_at, cancellation_reason, created_at)
VALUES ('TM-2025-000004', 1, 4, 'CANCELLED', 2, 134000.00, CURRENT_TIMESTAMP - INTERVAL '1 day', 'Изменились планы', CURRENT_TIMESTAMP - INTERVAL '7 days');

INSERT INTO passengers (booking_id, first_name, last_name, date_of_birth, gender)
VALUES 
(4, 'Дмитрий', 'Сидоров', '1988-07-30', 'MALE'),
(4, 'Елена', 'Сидорова', '1990-12-05', 'FEMALE');

