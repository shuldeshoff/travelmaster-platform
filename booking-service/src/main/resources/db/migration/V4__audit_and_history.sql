-- ================================================================
-- Booking Service - V4: Audit and History
-- ================================================================
-- Дата: 2025-10-31
-- Описание: Добавление audit таблиц и истории изменений
-- ================================================================

-- Таблица для истории изменений статусов бронирований
CREATE TABLE IF NOT EXISTS booking_status_history (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    old_status VARCHAR(20),
    new_status VARCHAR(20) NOT NULL,
    changed_by VARCHAR(100),
    change_reason VARCHAR(255),
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Индексы для status_history
CREATE INDEX idx_booking_status_history_booking_id ON booking_status_history(booking_id);
CREATE INDEX idx_booking_status_history_changed_at ON booking_status_history(changed_at DESC);
CREATE INDEX idx_booking_status_history_new_status ON booking_status_history(new_status);

-- Таблица для audit trail
CREATE TABLE IF NOT EXISTS booking_audit_trail (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT REFERENCES bookings(id) ON DELETE CASCADE,
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50),
    entity_id BIGINT,
    old_value JSONB,
    new_value JSONB,
    user_id BIGINT,
    ip_address VARCHAR(45),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Индексы для audit_trail
CREATE INDEX idx_booking_audit_booking_id ON booking_audit_trail(booking_id);
CREATE INDEX idx_booking_audit_action ON booking_audit_trail(action);
CREATE INDEX idx_booking_audit_created_at ON booking_audit_trail(created_at DESC);
CREATE INDEX idx_booking_audit_user_id ON booking_audit_trail(user_id);

-- GIN индекс для JSONB полей
CREATE INDEX idx_booking_audit_old_value_gin ON booking_audit_trail USING gin(old_value);
CREATE INDEX idx_booking_audit_new_value_gin ON booking_audit_trail USING gin(new_value);

-- Триггер для автоматического логирования изменений статуса
CREATE OR REPLACE FUNCTION log_booking_status_change()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.status IS DISTINCT FROM OLD.status THEN
        INSERT INTO booking_status_history (booking_id, old_status, new_status, changed_by, change_reason)
        VALUES (NEW.id, OLD.status, NEW.status, CURRENT_USER, 'Status changed');
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_booking_status_change
AFTER UPDATE ON bookings
FOR EACH ROW
EXECUTE FUNCTION log_booking_status_change();

-- Комментарии
COMMENT ON TABLE booking_status_history IS 'История изменений статусов бронирований';
COMMENT ON TABLE booking_audit_trail IS 'Полный audit trail всех операций с бронированиями';
COMMENT ON TRIGGER trg_booking_status_change ON bookings IS 'Автоматическое логирование изменений статуса';

