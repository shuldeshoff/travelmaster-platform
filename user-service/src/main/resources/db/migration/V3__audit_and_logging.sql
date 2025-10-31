-- ================================================================
-- User Service - V3: Audit and Logging
-- ================================================================
-- Дата: 2025-10-31
-- Описание: Добавление audit таблиц для отслеживания изменений
-- ================================================================

-- Таблица для аудита действий пользователей
CREATE TABLE IF NOT EXISTS user_audit_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(100),
    entity_id VARCHAR(100),
    old_value TEXT,
    new_value TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Индексы для audit_log
CREATE INDEX idx_user_audit_log_user_id ON user_audit_log(user_id);
CREATE INDEX idx_user_audit_log_action ON user_audit_log(action);
CREATE INDEX idx_user_audit_log_created_at ON user_audit_log(created_at DESC);
CREATE INDEX idx_user_audit_log_entity ON user_audit_log(entity_type, entity_id);

-- Таблица для истории входов
CREATE TABLE IF NOT EXISTS login_history (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    login_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    user_agent TEXT,
    success BOOLEAN NOT NULL,
    failure_reason VARCHAR(255),
    session_id VARCHAR(255)
);

-- Индексы для login_history
CREATE INDEX idx_login_history_user_id ON login_history(user_id);
CREATE INDEX idx_login_history_login_time ON login_history(login_time DESC);
CREATE INDEX idx_login_history_success ON login_history(success);
CREATE INDEX idx_login_history_ip ON login_history(ip_address);

-- Комментарии
COMMENT ON TABLE user_audit_log IS 'Audit log всех действий пользователей';
COMMENT ON TABLE login_history IS 'История входов в систему для security анализа';

