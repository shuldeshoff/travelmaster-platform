-- Payment Service Database Schema

-- Создание таблицы платежей
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    payment_reference VARCHAR(36) NOT NULL UNIQUE,
    booking_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'RUB',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    payment_method VARCHAR(20) NOT NULL,
    
    -- Gateway info
    gateway_transaction_id VARCHAR(100),
    gateway_name VARCHAR(50),
    
    -- Card info (masked)
    card_last_four VARCHAR(4),
    card_brand VARCHAR(20),
    
    -- Refund info
    refund_amount DECIMAL(10, 2),
    refunded_at TIMESTAMP,
    refund_reason VARCHAR(500),
    
    -- Processing info
    processed_at TIMESTAMP,
    failed_at TIMESTAMP,
    failure_reason TEXT,
    retry_count INTEGER DEFAULT 0,
    
    -- Security
    ip_address VARCHAR(45),
    user_agent TEXT,
    
    -- Audit
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    
    CONSTRAINT chk_amount CHECK (amount > 0),
    CONSTRAINT chk_refund_amount CHECK (refund_amount IS NULL OR refund_amount >= 0),
    CONSTRAINT chk_retry_count CHECK (retry_count >= 0)
);

-- Индексы для оптимизации
CREATE INDEX idx_payment_booking_id ON payments(booking_id);
CREATE INDEX idx_payment_user_id ON payments(user_id);
CREATE INDEX idx_payment_status ON payments(status);
CREATE INDEX idx_payment_reference ON payments(payment_reference);
CREATE INDEX idx_payment_gateway_tx ON payments(gateway_transaction_id);
CREATE INDEX idx_payment_created_at ON payments(created_at);
CREATE INDEX idx_payment_processed_at ON payments(processed_at);

-- Комментарии
COMMENT ON TABLE payments IS 'Платежи и транзакции';
COMMENT ON COLUMN payments.payment_reference IS 'Уникальный идентификатор платежа (UUID)';
COMMENT ON COLUMN payments.gateway_transaction_id IS 'ID транзакции в платежном шлюзе';
COMMENT ON COLUMN payments.card_last_four IS 'Последние 4 цифры карты (для отображения)';
COMMENT ON COLUMN payments.retry_count IS 'Количество попыток обработки';

-- Вставка тестовых данных
INSERT INTO payments (payment_reference, booking_id, user_id, amount, status, payment_method, gateway_transaction_id, gateway_name, card_last_four, card_brand, processed_at, created_at)
VALUES 
('550e8400-e29b-41d4-a716-446655440001', 1, 1, 90000.00, 'SUCCESS', 'CREDIT_CARD', 'TX-20251027-001', 'YooKassa', '4242', 'VISA', CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP - INTERVAL '4 days'),
('550e8400-e29b-41d4-a716-446655440002', 2, 1, 25000.00, 'PENDING', 'CREDIT_CARD', NULL, NULL, NULL, NULL, NULL, CURRENT_TIMESTAMP - INTERVAL '1 day'),
('550e8400-e29b-41d4-a716-446655440003', 3, 1, 89000.00, 'SUCCESS', 'DEBIT_CARD', 'TX-20251029-002', 'Tinkoff', '5678', 'MASTERCARD', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '2 days');

