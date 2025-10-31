-- Saga Log table for tracking distributed transactions

CREATE TABLE saga_logs (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    state VARCHAR(30) NOT NULL,
    step_name VARCHAR(100),
    step_description TEXT,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Индексы
CREATE INDEX idx_saga_booking_id ON saga_logs(booking_id);
CREATE INDEX idx_saga_state ON saga_logs(state);
CREATE INDEX idx_saga_created_at ON saga_logs(created_at);

-- Комментарии
COMMENT ON TABLE saga_logs IS 'Лог выполнения Saga для отслеживания распределенных транзакций';
COMMENT ON COLUMN saga_logs.booking_id IS 'ID бронирования, для которого выполняется Saga';
COMMENT ON COLUMN saga_logs.state IS 'Текущее состояние Saga';
COMMENT ON COLUMN saga_logs.step_name IS 'Название шага Saga';
COMMENT ON COLUMN saga_logs.step_description IS 'Описание выполненного шага';
COMMENT ON COLUMN saga_logs.error_message IS 'Сообщение об ошибке (если есть)';

