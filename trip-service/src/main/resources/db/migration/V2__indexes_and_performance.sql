-- ================================================================
-- Trip Service - V2: Indexes and Performance Optimization
-- ================================================================
-- Дата: 2025-10-31
-- Описание: Добавление индексов для оптимизации поиска и фильтрации
-- ================================================================

-- Индексы для Trip таблицы
CREATE INDEX IF NOT EXISTS idx_trips_status ON trips(status);
CREATE INDEX IF NOT EXISTS idx_trips_origin ON trips(origin);
CREATE INDEX IF NOT EXISTS idx_trips_destination ON trips(destination);
CREATE INDEX IF NOT EXISTS idx_trips_departure_date ON trips(departure_date);
CREATE INDEX IF NOT EXISTS idx_trips_created_at ON trips(created_at DESC);

-- Составные индексы для популярных запросов
CREATE INDEX IF NOT EXISTS idx_trips_search ON trips(origin, destination, departure_date) WHERE status = 'AVAILABLE';
CREATE INDEX IF NOT EXISTS idx_trips_status_departure ON trips(status, departure_date);
CREATE INDEX IF NOT EXISTS idx_trips_origin_destination ON trips(origin, destination);

-- GIN индекс для полнотекстового поиска (если нужен)
CREATE INDEX IF NOT EXISTS idx_trips_description_gin ON trips USING gin(to_tsvector('english', description));

-- Индексы для Segment таблицы
CREATE INDEX IF NOT EXISTS idx_segments_trip_id ON segments(trip_id);
CREATE INDEX IF NOT EXISTS idx_segments_type ON segments(type);
CREATE INDEX IF NOT EXISTS idx_segments_departure_time ON segments(departure_time);
CREATE INDEX IF NOT EXISTS idx_segments_arrival_time ON segments(arrival_time);

-- Составной индекс для сегментов поездки
CREATE INDEX IF NOT EXISTS idx_segments_trip_sequence ON segments(trip_id, sequence_number);

-- Комментарии
COMMENT ON INDEX idx_trips_search IS 'Составной индекс для быстрого поиска поездок';
COMMENT ON INDEX idx_trips_description_gin IS 'Full-text search по описанию поездки';
COMMENT ON INDEX idx_segments_trip_sequence IS 'Индекс для упорядоченного получения сегментов';

