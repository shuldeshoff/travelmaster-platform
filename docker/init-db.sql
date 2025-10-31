-- Инициализация баз данных для микросервисов TravelMaster Platform

-- Создание отдельных баз данных для каждого сервиса
CREATE DATABASE travelmaster_user;
CREATE DATABASE travelmaster_trip;
CREATE DATABASE travelmaster_booking;
CREATE DATABASE travelmaster_payment;
CREATE DATABASE travelmaster_notification;
CREATE DATABASE travelmaster_analytics;

-- Создание пользователей с правами (опционально, если нужна изоляция)
-- CREATE USER user_service WITH PASSWORD 'user_password';
-- GRANT ALL PRIVILEGES ON DATABASE travelmaster_user TO user_service;

-- Расширения для всех баз данных (если потребуются)
\c travelmaster_user;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\c travelmaster_trip;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\c travelmaster_booking;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\c travelmaster_payment;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\c travelmaster_notification;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\c travelmaster_analytics;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Возвращаемся в основную базу данных
\c travelmaster;

-- Логирование успешной инициализации
SELECT 'Database initialization completed successfully' AS status;

