-- ================================================================
-- User Service - V2: Indexes and Performance Optimization
-- ================================================================
-- Дата: 2025-10-31
-- Описание: Добавление индексов для оптимизации производительности
-- ================================================================

-- Индексы для User таблицы
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_deleted ON users(deleted) WHERE deleted = false;
CREATE INDEX IF NOT EXISTS idx_users_enabled ON users(enabled) WHERE enabled = true;
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_users_oauth ON users(oauth_provider, oauth_id) WHERE oauth_provider IS NOT NULL;

-- Составной индекс для частого запроса
CREATE INDEX IF NOT EXISTS idx_users_email_deleted ON users(email, deleted);
CREATE INDEX IF NOT EXISTS idx_users_enabled_deleted ON users(enabled, deleted);

-- Индексы для Role таблицы
CREATE INDEX IF NOT EXISTS idx_roles_name ON roles(name);

-- Индексы для связующей таблицы user_roles
CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_role_id ON user_roles(role_id);

-- Добавление комментариев
COMMENT ON INDEX idx_users_email IS 'Индекс для быстрого поиска по email';
COMMENT ON INDEX idx_users_deleted IS 'Partial index для активных пользователей';
COMMENT ON INDEX idx_users_email_deleted IS 'Составной индекс для login запросов';

