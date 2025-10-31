# TravelMaster Platform

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## 📝 Описание

**TravelMaster** — современная микросервисная платформа для бронирования и управления путешествиями. 

Проект демонстрирует промышленную архитектуру enterprise-уровня с использованием:
- ☕ Java 21 / Spring Boot 3
- 🏗️ Микросервисная архитектура
- 🔐 Безопасность и соответствие ФЗ-152
- ☁️ Cloud-native подход с Kubernetes
- 🚀 CI/CD автоматизация

## 🎯 Ключевые возможности

- ✅ **Управление пользователями** — регистрация, аутентификация, OAuth 2.0
- ✈️ **Поиск и бронирование** — интеграция с внешними провайдерами (Amadeus, Booking)
- 💳 **Платежи** — безопасная обработка с соответствием PCI DSS
- 📧 **Уведомления** — email, SMS, push через Kafka
- 📊 **Аналитика** — агрегация данных по бронированиям и доходам

## 🏗️ Архитектура

Система состоит из 7 микросервисов:

```
┌─────────────────┐
│  Gateway Service │ ← Единая точка входа, JWT auth
└────────┬────────┘
         │
    ┌────┴────┬────────┬──────────┬───────────┬─────────────┐
    │         │        │          │           │             │
┌───▼──┐  ┌──▼───┐  ┌─▼────┐  ┌──▼──────┐  ┌─▼───────┐  ┌─▼─────────┐
│ User │  │ Trip │  │Booking│  │ Payment │  │Notification│ │ Analytics │
└──────┘  └──────┘  └───────┘  └─────────┘  └──────────┘  └───────────┘
```

## 🛠️ Технологический стек

### Backend
- **Java 21** — современный LTS релиз
- **Spring Boot 3.x** — фреймворк для микросервисов
- **Spring Cloud Gateway** — API Gateway
- **Spring Security** — аутентификация и авторизация
- **Spring Data JPA** / Hibernate — ORM
- **MapStruct** — маппинг DTO
- **Lombok** — уменьшение boilerplate кода

### Базы данных
- **PostgreSQL 14** — основная СУБД
- **Redis** — кеширование и rate limiting
- **Flyway** — миграции БД

### Messaging & Events
- **Apache Kafka** — event streaming

### Infrastructure & DevOps
- **Docker** / Docker Compose — контейнеризация
- **Kubernetes** — оркестрация
- **Helm** — управление K8s манифестами
- **GitHub Actions** — CI/CD

### Monitoring & Observability
- **Prometheus** — сбор метрик
- **Grafana** — визуализация
- **Spring Actuator** — health checks и metrics
- **Micrometer** — distributed tracing

### Testing
- **JUnit 5** — unit тесты
- **Testcontainers** — integration тесты
- **Spring Boot Test** — тестирование Spring приложений

## 🚀 Быстрый старт

### Предварительные требования

- Java 21+
- Maven 3.8+ или Gradle 8+
- Docker & Docker Compose
- (опционально) Kubernetes cluster

### Локальный запуск

1. **Клонируйте репозиторий:**
```bash
git clone https://github.com/shuldeshoff/travelmaster-platform.git
cd travelmaster-platform
```

2. **Запустите инфраструктуру через Docker Compose:**
```bash
docker-compose up -d
```

Это запустит:
- PostgreSQL (порт 5432)
- Redis (порт 6379)
- Kafka + Zookeeper (порт 9092)
- pgAdmin (порт 5050)

3. **Соберите проект:**
```bash
./mvnw clean install
```

4. **Запустите сервисы:**
```bash
# Gateway Service
cd gateway-service && ./mvnw spring-boot:run

# User Service
cd user-service && ./mvnw spring-boot:run

# Другие сервисы аналогично...
```

5. **Проверьте работоспособность:**
```bash
curl http://localhost:8080/actuator/health
```

### Доступ к сервисам

- **API Gateway:** http://localhost:8080
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **pgAdmin:** http://localhost:5050
- **Prometheus:** http://localhost:9090
- **Grafana:** http://localhost:3000

## 📚 Документация

Подробная документация доступна в папке `/docs`:

- 📖 [План реализации](docs/IMPLEMENTATION_PLAN.md) — детальный roadmap проекта
- 🏗️ [Архитектура](docs/ARCHITECTURE.md) — схемы и диаграммы
- 🔌 [API спецификация](docs/API_SPEC.yaml) — OpenAPI 3.0
- 👥 [Руководство для команды](docs/TEAM_GUIDE.md) — git flow, code review
- 🔒 [Безопасность](docs/SECURITY.md) — ФЗ-152, PCI DSS compliance
- 🚢 [Деплой](docs/DEPLOYMENT.md) — инструкции по развертыванию

## 🧪 Тестирование

```bash
# Unit тесты
./mvnw test

# Integration тесты
./mvnw verify

# Coverage report
./mvnw jacoco:report
```

## 🐳 Docker

### Сборка образов

```bash
# Сборка всех сервисов
./mvnw spring-boot:build-image

# Или через Docker
docker build -t travelmaster/gateway-service:latest ./gateway-service
```

### Запуск через Docker Compose

```bash
docker-compose up -d
```

## ☸️ Kubernetes

### Деплой через kubectl

```bash
kubectl apply -f k8s/
```

### Деплой через Helm

```bash
helm install travelmaster ./helm/travelmaster -f helm/travelmaster/values-prod.yaml
```

## 🔐 Безопасность

Проект реализует следующие меры безопасности:

- ✅ JWT аутентификация
- ✅ OAuth 2.0 / OpenID Connect
- ✅ RBAC (Role-Based Access Control)
- ✅ Rate limiting
- ✅ HTTPS/TLS шифрование
- ✅ Защита от OWASP Top 10
- ✅ PCI DSS compliance для платежей
- ✅ ФЗ-152 соответствие для персональных данных
- ✅ Audit logging

## 📊 Мониторинг

Мониторинг осуществляется через:
- **Spring Actuator** — health checks
- **Prometheus** — сбор метрик
- **Grafana** — дашборды
- **Micrometer** — distributed tracing

## 🤝 Участие в разработке

1. Fork проекта
2. Создайте feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit изменения (`git commit -m 'Add some AmazingFeature'`)
4. Push в branch (`git push origin feature/AmazingFeature`)
5. Откройте Pull Request

Подробнее см. [TEAM_GUIDE.md](docs/TEAM_GUIDE.md)

## 📋 TODO / Roadmap

- [x] Базовая структура проекта
- [x] План реализации
- [ ] Gateway Service
- [ ] User Service
- [ ] Trip Service
- [ ] Booking Service
- [ ] Payment Service
- [ ] Notification Service
- [ ] Analytics Service
- [ ] Kubernetes деплой
- [ ] CI/CD pipeline

Подробный roadmap: [IMPLEMENTATION_PLAN.md](docs/IMPLEMENTATION_PLAN.md)

## 📄 Лицензия

Этот проект лицензирован под MIT License - см. файл [LICENSE](LICENSE) для деталей.

## 👤 Автор

**Tech Lead / Solution Architect**

- GitHub: [@shuldeshoff](https://github.com/shuldeshoff)

## 🙏 Благодарности

Проект создан как демонстрация современных практик разработки enterprise-приложений с использованием Java и Spring Boot.

---

⭐ **Если проект был полезен, поставьте звезду!** ⭐

