# Booking Service

Сервис управления бронированиями в TravelMaster Platform.

## Функциональность

### Основные возможности
- ✅ Создание бронирований с пассажирами
- ✅ State Machine для управления статусами бронирования
- ✅ Saga Pattern для распределенных транзакций
- ✅ Kafka интеграция для event-driven architecture
- ✅ Интеграция с Trip Service для резервирования мест
- ✅ Отмена бронирований с компенсирующими транзакциями
- ✅ Личные данные пассажиров (ФЗ-152)

### Статусы бронирования

```
PENDING → CONFIRMED → PAID → COMPLETED
   ↓          ↓         ↓
        CANCELLED
```

- **PENDING**: Создано, ожидает подтверждения
- **CONFIRMED**: Подтверждено, места зарезервированы
- **PAID**: Оплачено
- **COMPLETED**: Завершено (поездка состоялась)
- **CANCELLED**: Отменено

## Архитектура

### State Machine
Управление переходами между статусами реализовано через Spring State Machine:
- Гарантированные переходы между валидными статусами
- Валидация возможности выполнения операции
- Логирование всех изменений состояния

### Saga Pattern
Для надежных распределенных транзакций:

**Создание бронирования:**
1. Создание записи бронирования (локально)
2. Резервирование мест в Trip Service
3. Подтверждение бронирования

**Компенсирующие транзакции:**
- Освобождение мест при ошибке
- Возврат средств при отмене
- Логирование всех шагов Saga

### Event-Driven Communication
События публикуются в Kafka:
- `BookingCreatedEvent` - создано новое бронирование
- `BookingConfirmedEvent` - бронирование подтверждено
- `BookingPaidEvent` - бронирование оплачено
- `BookingCancelledEvent` - бронирование отменено
- `BookingCompletedEvent` - бронирование завершено

## API Endpoints

### Бронирования

```bash
# Создать бронирование
POST /api/v1/bookings
{
  "tripId": 1,
  "passengers": [
    {
      "firstName": "Иван",
      "lastName": "Иванов",
      "dateOfBirth": "1985-05-15",
      "passportNumber": "1234567890",
      "passportCountry": "RUS",
      "gender": "MALE",
      "email": "ivan@example.com"
    }
  ],
  "specialRequests": "Нужен багаж"
}

# Получить бронирование по ID
GET /api/v1/bookings/{id}

# Получить бронирование по номеру
GET /api/v1/bookings/reference/{reference}

# Мои бронирования
GET /api/v1/bookings/my?page=0&size=10

# Мои бронирования по статусу
GET /api/v1/bookings/my/status/CONFIRMED

# Подтвердить бронирование
POST /api/v1/bookings/{id}/confirm

# Отменить бронирование
POST /api/v1/bookings/{id}/cancel?reason=Изменились планы

# Завершить бронирование
POST /api/v1/bookings/{id}/complete
```

## База данных

### Таблицы
- `bookings` - основная информация о бронировании
- `passengers` - данные пассажиров
- `saga_logs` - логи выполнения Saga

### Миграции
- `V1__initial_schema.sql` - начальная схема
- `V2__saga_logs.sql` - таблица логов Saga

## Технологии

- **Spring Boot 3.2.1**
- **Spring State Machine** - управление статусами
- **Spring Kafka** - event-driven messaging
- **PostgreSQL** - основная БД
- **Flyway** - миграции БД
- **MapStruct** - маппинг DTO
- **RestTemplate** - синхронная коммуникация с другими сервисами

## Безопасность и Compliance

### ФЗ-152 (Защита персональных данных)
- Хранение паспортных данных пассажиров
- Шифрование чувствительных данных
- Аудит доступа к персональным данным

### Лучшие практики
- Валидация всех входных данных
- Транзакционная целостность
- Компенсирующие транзакции при ошибках
- Логирование всех операций

## Запуск

### Локально
```bash
mvn spring-boot:run
```

### Docker
```bash
docker build -t booking-service .
docker run -p 8083:8083 booking-service
```

### С Docker Compose
```bash
# Из корня проекта
docker-compose up booking-service
```

## Конфигурация

### application.yml
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/bookingdb
  kafka:
    bootstrap-servers: localhost:9092
```

### application-docker.yml
```yaml
spring:
  datasource:
    url: jdbc:postgresql://postgres:5432/bookingdb
  kafka:
    bootstrap-servers: kafka:9092
```

## Интеграции

### Trip Service
- `GET /api/v1/trips/{id}` - получение информации о поездке
- `POST /api/v1/trips/{id}/reserve` - резервирование мест
- `POST /api/v1/trips/{id}/release` - освобождение мест

### Payment Service (в разработке)
- Инициация платежа
- Подтверждение оплаты
- Возврат средств

## Мониторинг

- **Health**: `GET /actuator/health`
- **Metrics**: `GET /actuator/metrics`
- **Prometheus**: `GET /actuator/prometheus`

## TODO

- [ ] Интеграция с Payment Service
- [ ] Автоматическое завершение бронирований после окончания поездки
- [ ] Email/SMS уведомления через Notification Service
- [ ] Расширенная валидация паспортных данных
- [ ] Политики возврата средств
- [ ] Тесты (Unit + Integration)

