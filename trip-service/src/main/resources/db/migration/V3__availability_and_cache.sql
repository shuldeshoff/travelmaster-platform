-- ================================================================
-- Trip Service - V3: Availability Tracking and Cache
-- ================================================================
-- Дата: 2025-10-31
-- Описание: Добавление таблиц для отслеживания доступности и кеширования
-- ================================================================

-- Таблица для отслеживания изменений доступности
CREATE TABLE IF NOT EXISTS trip_availability_log (
    id BIGSERIAL PRIMARY KEY,
    trip_id BIGINT NOT NULL REFERENCES trips(id) ON DELETE CASCADE,
    previous_available_seats INT,
    new_available_seats INT,
    change_reason VARCHAR(100),
    changed_by VARCHAR(100),
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Индексы для availability_log
CREATE INDEX idx_trip_availability_trip_id ON trip_availability_log(trip_id);
CREATE INDEX idx_trip_availability_changed_at ON trip_availability_log(changed_at DESC);

-- Материализованное представление для популярных направлений
CREATE MATERIALIZED VIEW IF NOT EXISTS popular_routes AS
SELECT 
    origin,
    destination,
    COUNT(*) as trip_count,
    AVG(base_price) as avg_price,
    MIN(base_price) as min_price,
    MAX(base_price) as max_price
FROM trips
WHERE status = 'AVAILABLE'
  AND departure_date > CURRENT_DATE
GROUP BY origin, destination
HAVING COUNT(*) > 0;

-- Индекс для materialized view
CREATE INDEX idx_popular_routes_origin_dest ON popular_routes(origin, destination);

-- Функция для обновления materialized view
CREATE OR REPLACE FUNCTION refresh_popular_routes()
RETURNS void AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY popular_routes;
END;
$$ LANGUAGE plpgsql;

-- Комментарии
COMMENT ON TABLE trip_availability_log IS 'История изменений доступности мест в поездках';
COMMENT ON MATERIALIZED VIEW popular_routes IS 'Кеш популярных маршрутов для быстрого доступа';

