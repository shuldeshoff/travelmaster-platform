-- Notification Service Database Schema

-- Создание таблицы уведомлений
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    recipient_email VARCHAR(255) NOT NULL,
    recipient_phone VARCHAR(20),
    type VARCHAR(20) NOT NULL,
    channel VARCHAR(20) NOT NULL DEFAULT 'EMAIL',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    subject VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    template_name VARCHAR(100),
    template_variables TEXT,
    
    -- Related entities
    booking_id BIGINT,
    payment_id BIGINT,
    trip_id BIGINT,
    
    -- Processing info
    sent_at TIMESTAMP,
    failed_at TIMESTAMP,
    error_message TEXT,
    retry_count INTEGER DEFAULT 0,
    next_retry_at TIMESTAMP,
    
    -- Audit
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    
    CONSTRAINT chk_retry_count CHECK (retry_count >= 0 AND retry_count <= 3)
);

-- Индексы для оптимизации
CREATE INDEX idx_notification_user_id ON notifications(user_id);
CREATE INDEX idx_notification_status ON notifications(status);
CREATE INDEX idx_notification_type ON notifications(type);
CREATE INDEX idx_notification_created_at ON notifications(created_at);
CREATE INDEX idx_notification_next_retry ON notifications(next_retry_at) WHERE status = 'FAILED';
CREATE INDEX idx_notification_booking_id ON notifications(booking_id);
CREATE INDEX idx_notification_payment_id ON notifications(payment_id);

-- Комментарии
COMMENT ON TABLE notifications IS 'Уведомления пользователям (Email, SMS, Push)';
COMMENT ON COLUMN notifications.template_variables IS 'JSON с переменными для шаблона';
COMMENT ON COLUMN notifications.retry_count IS 'Количество попыток отправки (максимум 3)';
COMMENT ON COLUMN notifications.next_retry_at IS 'Время следующей попытки отправки';

-- Вставка тестовых данных
INSERT INTO notifications (user_id, recipient_email, type, channel, status, subject, content, booking_id, sent_at, created_at)
VALUES 
(1, 'admin@example.com', 'BOOKING_CREATED', 'EMAIL', 'SENT', 
 'Ваше бронирование создано', 
 'Здравствуйте! Ваше бронирование TM-2025-000001 успешно создано.', 
 1, CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP - INTERVAL '3 days'),

(1, 'admin@example.com', 'BOOKING_CONFIRMED', 'EMAIL', 'SENT', 
 'Ваше бронирование подтверждено', 
 'Здравствуйте! Ваше бронирование TM-2025-000001 подтверждено. Места зарезервированы.', 
 1, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days'),

(1, 'admin@example.com', 'PAYMENT_SUCCESS', 'EMAIL', 'SENT', 
 'Платеж успешно обработан', 
 'Здравствуйте! Ваш платеж на сумму 90000.00 RUB успешно обработан.', 
 1, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days');

