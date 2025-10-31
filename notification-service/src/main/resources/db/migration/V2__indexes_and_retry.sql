-- ================================================================
-- Notification Service - V2: Indexes and Retry Mechanism
-- ================================================================
-- Дата: 2025-10-31
-- Описание: Добавление индексов и механизма повторных отправок
-- ================================================================

-- Индексы для Notification таблицы
CREATE INDEX IF NOT EXISTS idx_notifications_recipient_email ON notifications(recipient_email);
CREATE INDEX IF NOT EXISTS idx_notifications_type ON notifications(type);
CREATE INDEX IF NOT EXISTS idx_notifications_channel ON notifications(channel);
CREATE INDEX IF NOT EXISTS idx_notifications_status ON notifications(status);
CREATE INDEX IF NOT EXISTS idx_notifications_created_at ON notifications(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_notifications_sent_at ON notifications(sent_at DESC);

-- Составные индексы для частых запросов
CREATE INDEX IF NOT EXISTS idx_notifications_status_channel ON notifications(status, channel);
CREATE INDEX IF NOT EXISTS idx_notifications_recipient_status ON notifications(recipient_email, status);

-- Partial индекс для failed notifications (для retry)
CREATE INDEX IF NOT EXISTS idx_notifications_failed_retry ON notifications(status, retry_count, created_at)
WHERE status = 'FAILED' AND retry_count < 3;

-- Partial индекс для pending notifications
CREATE INDEX IF NOT EXISTS idx_notifications_pending ON notifications(status, created_at)
WHERE status = 'PENDING';

-- GIN индекс для поиска в content
CREATE INDEX IF NOT EXISTS idx_notifications_content_gin ON notifications USING gin(to_tsvector('english', content));

-- Таблица для retry history
CREATE TABLE IF NOT EXISTS notification_retry_history (
    id BIGSERIAL PRIMARY KEY,
    notification_id BIGINT NOT NULL REFERENCES notifications(id) ON DELETE CASCADE,
    retry_attempt INT NOT NULL,
    retry_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    error_message TEXT,
    next_retry_at TIMESTAMP
);

-- Индексы для retry_history
CREATE INDEX idx_notification_retry_notification_id ON notification_retry_history(notification_id);
CREATE INDEX idx_notification_retry_retry_at ON notification_retry_history(retry_at DESC);
CREATE INDEX idx_notification_retry_next_retry_at ON notification_retry_history(next_retry_at)
WHERE next_retry_at IS NOT NULL;

-- Функция для определения следующего времени retry (exponential backoff)
CREATE OR REPLACE FUNCTION calculate_next_retry(attempt INT)
RETURNS TIMESTAMP AS $$
BEGIN
    -- Exponential backoff: 1 min, 5 min, 30 min
    RETURN CURRENT_TIMESTAMP + (POWER(5, attempt) || ' minutes')::INTERVAL;
END;
$$ LANGUAGE plpgsql;

-- Комментарии
COMMENT ON INDEX idx_notifications_failed_retry IS 'Partial index для поиска уведомлений для повторной отправки';
COMMENT ON TABLE notification_retry_history IS 'История попыток отправки уведомлений';
COMMENT ON FUNCTION calculate_next_retry IS 'Расчет времени следующей попытки с exponential backoff';

