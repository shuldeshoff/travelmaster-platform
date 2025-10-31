-- ================================================================
-- Payment Service - V3: Audit, Refunds and Compliance
-- ================================================================
-- Дата: 2025-10-31
-- Описание: Добавление audit таблиц, refunds и compliance
-- ================================================================

-- Таблица для refunds (возвраты)
CREATE TABLE IF NOT EXISTS payment_refunds (
    id BIGSERIAL PRIMARY KEY,
    payment_id BIGINT NOT NULL REFERENCES payments(id) ON DELETE RESTRICT,
    refund_amount DECIMAL(10, 2) NOT NULL,
    refund_reason VARCHAR(255),
    refund_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    refund_transaction_id VARCHAR(255),
    requested_by BIGINT,
    requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    completed_at TIMESTAMP,
    failure_reason TEXT,
    
    CONSTRAINT chk_refund_amount_positive CHECK (refund_amount > 0),
    CONSTRAINT chk_refund_status CHECK (refund_status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED'))
);

-- Индексы для refunds
CREATE INDEX idx_payment_refunds_payment_id ON payment_refunds(payment_id);
CREATE INDEX idx_payment_refunds_status ON payment_refunds(refund_status);
CREATE INDEX idx_payment_refunds_requested_at ON payment_refunds(requested_at DESC);
CREATE UNIQUE INDEX idx_payment_refunds_transaction_id ON payment_refunds(refund_transaction_id) 
WHERE refund_transaction_id IS NOT NULL;

-- Таблица для payment audit log (PCI DSS compliance)
CREATE TABLE IF NOT EXISTS payment_audit_log (
    id BIGSERIAL PRIMARY KEY,
    payment_id BIGINT REFERENCES payments(id) ON DELETE CASCADE,
    action VARCHAR(50) NOT NULL,
    old_status VARCHAR(20),
    new_status VARCHAR(20),
    metadata JSONB,
    user_id BIGINT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Индексы для audit_log
CREATE INDEX idx_payment_audit_payment_id ON payment_audit_log(payment_id);
CREATE INDEX idx_payment_audit_action ON payment_audit_log(action);
CREATE INDEX idx_payment_audit_created_at ON payment_audit_log(created_at DESC);
CREATE INDEX idx_payment_audit_user_id ON payment_audit_log(user_id);

-- GIN индекс для JSONB metadata
CREATE INDEX idx_payment_audit_metadata_gin ON payment_audit_log USING gin(metadata);

-- Таблица для tracking gateway responses
CREATE TABLE IF NOT EXISTS payment_gateway_log (
    id BIGSERIAL PRIMARY KEY,
    payment_id BIGINT REFERENCES payments(id) ON DELETE CASCADE,
    gateway_name VARCHAR(50) NOT NULL,
    request_payload JSONB,
    response_payload JSONB,
    response_code VARCHAR(10),
    response_message TEXT,
    processing_time_ms INT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Индексы для gateway_log
CREATE INDEX idx_payment_gateway_payment_id ON payment_gateway_log(payment_id);
CREATE INDEX idx_payment_gateway_created_at ON payment_gateway_log(created_at DESC);
CREATE INDEX idx_payment_gateway_response_code ON payment_gateway_log(response_code);

-- Триггер для автоматического audit logging
CREATE OR REPLACE FUNCTION log_payment_change()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO payment_audit_log (payment_id, action, old_status, new_status, metadata)
    VALUES (
        NEW.id, 
        TG_OP, 
        OLD.status, 
        NEW.status,
        jsonb_build_object(
            'old_amount', OLD.amount,
            'new_amount', NEW.amount,
            'payment_method', NEW.payment_method
        )
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_payment_audit
AFTER UPDATE ON payments
FOR EACH ROW
WHEN (OLD.status IS DISTINCT FROM NEW.status)
EXECUTE FUNCTION log_payment_change();

-- Комментарии
COMMENT ON TABLE payment_refunds IS 'Таблица возвратов платежей с полной историей';
COMMENT ON TABLE payment_audit_log IS 'Audit log для PCI DSS compliance';
COMMENT ON TABLE payment_gateway_log IS 'Логирование всех взаимодействий с payment gateway';
COMMENT ON TRIGGER trg_payment_audit ON payments IS 'Автоматическое логирование изменений статуса платежа';

