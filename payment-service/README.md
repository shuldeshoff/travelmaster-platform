# Payment Service

Сервис обработки платежей в TravelMaster Platform.

## Функциональность

### Основные возможности
- ✅ Обработка платежей через платежные шлюзы
- ✅ Circuit Breaker для отказоустойчивости
- ✅ Retry механизм для повторных попыток
- ✅ Kafka events для событийной архитектуры
- ✅ Поддержка возвратов (refunds)
- ✅ Безопасное хранение данных карт (masked)
- ✅ PCI DSS compliance

### Статусы платежа

```
PENDING → PROCESSING → SUCCESS
            ↓
          FAILED (retry up to 3 times)

SUCCESS → REFUNDED
```

- **PENDING**: Создан, ожидает обработки
- **PROCESSING**: В процессе обработки через шлюз
- **SUCCESS**: Успешно обработан
- **FAILED**: Ошибка при обработке
- **REFUNDED**: Возвращен
- **CANCELLED**: Отменен

### Методы оплаты

- **CREDIT_CARD**: Кредитная карта
- **DEBIT_CARD**: Дебетовая карта
- **SBP**: Система быстрых платежей
- **BANK_TRANSFER**: Банковский перевод
- **WALLET**: Электронный кошелек

## Архитектура

### Circuit Breaker Pattern
Защита от каскадных сбоев при недоступности платежного шлюза:

**Конфигурация:**
- Sliding window: 10 вызовов
- Failure rate threshold: 50%
- Wait duration: 10 секунд
- Half-open state: 3 пробных вызова

### Retry Pattern
Автоматические повторные попытки:
- Max attempts: 3
- Wait duration: 1s
- Exponential backoff: 2x

### Event-Driven Communication
События публикуются в Kafka:
- `PaymentCreatedEvent` - создан новый платеж
- `PaymentProcessedEvent` - платеж успешно обработан
- `PaymentFailedEvent` - ошибка при обработке
- `PaymentRefundedEvent` - платеж возвращен

## API Endpoints

### Платежи

```bash
# Создать платеж
POST /api/v1/payments
{
  "bookingId": 1,
  "amount": 90000.00,
  "currency": "RUB",
  "paymentMethod": "CREDIT_CARD",
  "cardNumber": "4242424242424242",
  "cardHolderName": "IVAN IVANOV",
  "expiryMonth": "12",
  "expiryYear": "25",
  "cvv": "123"
}

# Получить платеж по ID
GET /api/v1/payments/{id}

# Получить платеж по номеру
GET /api/v1/payments/reference/{reference}

# Получить платеж по бронированию
GET /api/v1/payments/booking/{bookingId}

# Мои платежи
GET /api/v1/payments/my?page=0&size=10

# Вернуть платеж
POST /api/v1/payments/{id}/refund?reason=Отмена бронирования

# Повторить платеж
POST /api/v1/payments/{id}/retry
```

## База данных

### Таблицы
- `payments` - информация о платежах

### Миграции
- `V1__initial_schema.sql` - начальная схема

## Технологии

- **Spring Boot 3.2.1**
- **Resilience4j** - Circuit Breaker и Retry
- **Spring Kafka** - event-driven messaging
- **PostgreSQL** - основная БД
- **Flyway** - миграции БД
- **MapStruct** - маппинг DTO

## Безопасность и Compliance

### PCI DSS (Payment Card Industry Data Security Standard)
- ❌ Не хранится полный номер карты
- ✅ Только последние 4 цифры для отображения
- ✅ CVV никогда не сохраняется
- ✅ Шифрование в transit (TLS 1.3)
- ✅ IP-адрес и User-Agent для фрод-детекции

### Лучшие практики
- Токенизация карт (в production)
- 3D Secure поддержка (в production)
- Фрод-детекция
- Rate limiting для защиты от атак
- Аудит всех транзакций

## Интеграции

### Payment Gateways
- **MockPaymentGateway** - для тестирования
- **YooKassa** (TODO) - российский PSP
- **Tinkoff** (TODO) - банк-эквайер
- **Stripe** (TODO) - международные платежи

## Мониторинг

- **Health**: `GET /actuator/health`
- **Metrics**: `GET /actuator/metrics`
- **Prometheus**: `GET /actuator/prometheus`
- **Circuit Breaker Status**: `GET /actuator/circuitbreakers`

## Запуск

### Локально
```bash
mvn spring-boot:run
```

### Docker
```bash
docker build -t payment-service .
docker run -p 8084:8084 payment-service
```

### С Docker Compose
```bash
# Из корня проекта
docker-compose up payment-service
```

## Конфигурация

### application.yml
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/paymentdb
  kafka:
    bootstrap-servers: localhost:9092

resilience4j:
  circuitbreaker:
    instances:
      paymentGateway:
        failureRateThreshold: 50
        waitDurationInOpenState: 10s
```

## TODO

- [ ] Интеграция с реальными платежными шлюзами
- [ ] 3D Secure поддержка
- [ ] Автоматическое reconciliation
- [ ] Scheduled jobs для повторных попыток
- [ ] Webhook endpoints для уведомлений от шлюзов
- [ ] Тесты (Unit + Integration)

