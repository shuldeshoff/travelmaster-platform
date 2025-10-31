-- Создание таблицы ролей
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200)
);

-- Создание таблицы пользователей
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    phone_number VARCHAR(20),
    passport_number VARCHAR(50),
    enabled BOOLEAN NOT NULL DEFAULT true,
    deleted BOOLEAN NOT NULL DEFAULT false,
    deleted_at TIMESTAMP,
    oauth_provider VARCHAR(50),
    oauth_id VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

-- Создание таблицы связи пользователей и ролей
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Индексы для оптимизации запросов
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_oauth ON users(oauth_provider, oauth_id);
CREATE INDEX idx_users_deleted ON users(deleted);

-- Вставка начальных ролей
INSERT INTO roles (name, description) VALUES
    ('ROLE_TRAVELER', 'Обычный пользователь - путешественник'),
    ('ROLE_AGENT', 'Турагент с расширенными правами'),
    ('ROLE_ADMIN', 'Администратор системы');

-- Создание admin пользователя (пароль: admin123)
-- Пароль захэширован с BCrypt strength 12
INSERT INTO users (email, password, first_name, last_name, enabled)
VALUES ('admin@travelmaster.com',
        '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYRq1N4.8yG',
        'Admin',
        'User',
        true);

-- Назначение роли ADMIN администратору
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'admin@travelmaster.com'
  AND r.name = 'ROLE_ADMIN';

