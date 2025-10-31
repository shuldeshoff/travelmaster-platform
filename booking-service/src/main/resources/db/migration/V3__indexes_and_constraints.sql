-- ================================================================
-- Booking Service - V3: Indexes and Performance Optimization
-- ================================================================
-- Дата: 2025-10-31
-- Описание: Добавление индексов для оптимизации производительности
-- ================================================================

-- Индексы для Booking таблицы
CREATE INDEX IF NOT EXISTS idx_bookings_user_id ON bookings(user_id);
CREATE INDEX IF NOT EXISTS idx_bookings_trip_id ON bookings(trip_id);
CREATE INDEX IF NOT EXISTS idx_bookings_status ON bookings(status);
CREATE INDEX IF NOT EXISTS idx_bookings_booking_number ON bookings(booking_number);
CREATE INDEX IF NOT EXISTS idx_bookings_created_at ON bookings(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_bookings_booking_date ON bookings(booking_date DESC);

-- Составные индексы для частых запросов
CREATE INDEX IF NOT EXISTS idx_bookings_user_status ON bookings(user_id, status);
CREATE INDEX IF NOT EXISTS idx_bookings_status_date ON bookings(status, booking_date);
CREATE INDEX IF NOT EXISTS idx_bookings_trip_status ON bookings(trip_id, status);

-- Уникальный индекс для booking_number
CREATE UNIQUE INDEX IF NOT EXISTS idx_bookings_booking_number_unique ON bookings(booking_number);

-- Индексы для Passenger таблицы
CREATE INDEX IF NOT EXISTS idx_passengers_booking_id ON passengers(booking_id);
CREATE INDEX IF NOT EXISTS idx_passengers_passport ON passengers(passport_number);
CREATE INDEX IF NOT EXISTS idx_passengers_email ON passengers(email);

-- Индексы для Saga Log таблицы
CREATE INDEX IF NOT EXISTS idx_saga_logs_saga_id ON saga_logs(saga_id);
CREATE INDEX IF NOT EXISTS idx_saga_logs_state ON saga_logs(state);
CREATE INDEX IF NOT EXISTS idx_saga_logs_created_at ON saga_logs(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_saga_logs_updated_at ON saga_logs(updated_at DESC);

-- Partial index для активных саг
CREATE INDEX IF NOT EXISTS idx_saga_logs_active ON saga_logs(saga_id, state) 
WHERE state NOT IN ('COMPLETED', 'FAILED', 'COMPENSATED');

-- Комментарии
COMMENT ON INDEX idx_bookings_user_status IS 'Индекс для запросов бронирований пользователя по статусу';
COMMENT ON INDEX idx_saga_logs_active IS 'Partial index для мониторинга активных саг';

