-- ================================================================
-- Payment Service - V2: Indexes and Security
-- ================================================================
-- Дата: 2025-10-31
-- Описание: Добавление индексов и security constraints
-- ================================================================

-- Индексы для Payment таблицы
CREATE INDEX IF NOT EXISTS idx_payments_booking_id ON payments(booking_id);
CREATE INDEX IF NOT EXISTS idx_payments_user_id ON payments(user_id);
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(status);
CREATE INDEX IF NOT EXISTS idx_payments_transaction_id ON payments(transaction_id);
CREATE INDEX IF NOT EXISTS idx_payments_payment_method ON payments(payment_method);
CREATE INDEX IF NOT EXISTS idx_payments_created_at ON payments(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_payments_processed_at ON payments(processed_at DESC);

-- Уникальный индекс для transaction_id
CREATE UNIQUE INDEX IF NOT EXISTS idx_payments_transaction_id_unique ON payments(transaction_id);

-- Составные индексы для отчетов
CREATE INDEX IF NOT EXISTS idx_payments_user_status ON payments(user_id, status);
CREATE INDEX IF NOT EXISTS idx_payments_status_date ON payments(status, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_payments_method_status ON payments(payment_method, status);

-- Partial индексы для оптимизации
CREATE INDEX IF NOT EXISTS idx_payments_pending ON payments(booking_id, status) 
WHERE status = 'PENDING';

CREATE INDEX IF NOT EXISTS idx_payments_failed ON payments(user_id, status, created_at) 
WHERE status = 'FAILED';

-- Индекс для поиска платежей за период
CREATE INDEX IF NOT EXISTS idx_payments_date_range ON payments(created_at, status)
WHERE status = 'COMPLETED';

-- Добавление constraint для проверки суммы
ALTER TABLE payments 
ADD CONSTRAINT chk_payments_amount_positive 
CHECK (amount > 0);

-- Добавление constraint для currency
ALTER TABLE payments 
ADD CONSTRAINT chk_payments_currency 
CHECK (currency IN ('RUB', 'USD', 'EUR', 'GBP'));

-- Комментарии
COMMENT ON INDEX idx_payments_transaction_id_unique IS 'Гарантирует уникальность transaction_id для предотвращения дубликатов';
COMMENT ON INDEX idx_payments_pending IS 'Partial index для быстрого поиска pending платежей';
COMMENT ON CONSTRAINT chk_payments_amount_positive ON payments IS 'Сумма платежа должна быть положительной';

