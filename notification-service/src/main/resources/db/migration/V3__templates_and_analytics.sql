-- ================================================================
-- Notification Service - V3: Templates and Analytics
-- ================================================================
-- Дата: 2025-10-31
-- Описание: Добавление templates и аналитики
-- ================================================================

-- Таблица для email/sms templates
CREATE TABLE IF NOT EXISTS notification_templates (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    type VARCHAR(50) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    subject VARCHAR(255),
    template_content TEXT NOT NULL,
    variables JSONB,
    language VARCHAR(5) DEFAULT 'en',
    is_active BOOLEAN DEFAULT true,
    version INT DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_template_type CHECK (type IN ('BOOKING_CREATED', 'BOOKING_CONFIRMED', 'PAYMENT_SUCCESS', 'PAYMENT_FAILED', 'BOOKING_CANCELLED')),
    CONSTRAINT chk_template_channel CHECK (channel IN ('EMAIL', 'SMS', 'PUSH'))
);

-- Индексы для templates
CREATE INDEX idx_notification_templates_name ON notification_templates(name);
CREATE INDEX idx_notification_templates_type ON notification_templates(type);
CREATE INDEX idx_notification_templates_channel ON notification_templates(channel);
CREATE INDEX idx_notification_templates_active ON notification_templates(is_active) WHERE is_active = true;

-- GIN индекс для JSONB variables
CREATE INDEX idx_notification_templates_variables_gin ON notification_templates USING gin(variables);

-- Таблица для статистики отправок
CREATE TABLE IF NOT EXISTS notification_statistics (
    id BIGSERIAL PRIMARY KEY,
    date DATE NOT NULL,
    channel VARCHAR(20) NOT NULL,
    type VARCHAR(50) NOT NULL,
    total_sent INT DEFAULT 0,
    total_delivered INT DEFAULT 0,
    total_failed INT DEFAULT 0,
    total_retry INT DEFAULT 0,
    avg_delivery_time_seconds INT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uq_notification_stats UNIQUE (date, channel, type)
);

-- Индексы для statistics
CREATE INDEX idx_notification_statistics_date ON notification_statistics(date DESC);
CREATE INDEX idx_notification_statistics_channel ON notification_statistics(channel);
CREATE INDEX idx_notification_statistics_type ON notification_statistics(type);

-- Материализованное представление для дашборда
CREATE MATERIALIZED VIEW IF NOT EXISTS notification_dashboard AS
SELECT 
    channel,
    type,
    COUNT(*) as total_notifications,
    COUNT(CASE WHEN status = 'SENT' THEN 1 END) as sent_count,
    COUNT(CASE WHEN status = 'FAILED' THEN 1 END) as failed_count,
    ROUND(AVG(retry_count), 2) as avg_retry_count,
    ROUND(
        AVG(EXTRACT(EPOCH FROM (sent_at - created_at))) / 60, 
        2
    ) as avg_delivery_time_minutes
FROM notifications
WHERE created_at > CURRENT_DATE - INTERVAL '7 days'
GROUP BY channel, type;

-- Индекс для materialized view
CREATE INDEX idx_notification_dashboard_channel ON notification_dashboard(channel);

-- Функция для обновления статистики
CREATE OR REPLACE FUNCTION update_notification_statistics()
RETURNS void AS $$
BEGIN
    INSERT INTO notification_statistics (date, channel, type, total_sent, total_delivered, total_failed)
    SELECT 
        CURRENT_DATE,
        channel,
        type,
        COUNT(*) as total_sent,
        COUNT(CASE WHEN status = 'SENT' THEN 1 END) as total_delivered,
        COUNT(CASE WHEN status = 'FAILED' THEN 1 END) as total_failed
    FROM notifications
    WHERE DATE(created_at) = CURRENT_DATE
    GROUP BY channel, type
    ON CONFLICT (date, channel, type) 
    DO UPDATE SET
        total_sent = EXCLUDED.total_sent,
        total_delivered = EXCLUDED.total_delivered,
        total_failed = EXCLUDED.total_failed,
        updated_at = CURRENT_TIMESTAMP;
END;
$$ LANGUAGE plpgsql;

-- Функция для refresh materialized view
CREATE OR REPLACE FUNCTION refresh_notification_dashboard()
RETURNS void AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY notification_dashboard;
END;
$$ LANGUAGE plpgsql;

-- Вставка дефолтных templates
INSERT INTO notification_templates (name, type, channel, subject, template_content, variables, language) VALUES
('booking-created-email', 'BOOKING_CREATED', 'EMAIL', 'Booking Confirmation - {{bookingNumber}}', 
 '<h1>Booking Confirmed</h1><p>Your booking {{bookingNumber}} has been created.</p>', 
 '["bookingNumber", "userName", "tripDetails"]', 'en'),
 
('payment-success-email', 'PAYMENT_SUCCESS', 'EMAIL', 'Payment Successful - {{amount}}', 
 '<h1>Payment Confirmed</h1><p>Your payment of {{amount}} {{currency}} has been processed.</p>', 
 '["amount", "currency", "transactionId"]', 'en'),
 
('booking-cancelled-email', 'BOOKING_CANCELLED', 'EMAIL', 'Booking Cancelled - {{bookingNumber}}', 
 '<h1>Booking Cancelled</h1><p>Your booking {{bookingNumber}} has been cancelled.</p>', 
 '["bookingNumber", "refundAmount", "reason"]', 'en')
ON CONFLICT (name) DO NOTHING;

-- Комментарии
COMMENT ON TABLE notification_templates IS 'Шаблоны для email/SMS уведомлений';
COMMENT ON TABLE notification_statistics IS 'Агрегированная статистика отправок';
COMMENT ON MATERIALIZED VIEW notification_dashboard IS 'Dashboard метрики за последние 7 дней';
COMMENT ON FUNCTION update_notification_statistics IS 'Обновление daily статистики';

