# Performance Tests

Performance and load testing для TravelMaster Platform с использованием Gatling.

## 🎯 Что Тестируем

### Load Tests
- **TripServiceLoadTest**: Baseline load testing для Trip Service
  - Search trips
  - View trip details
  - List all trips
  - Target: 50-100 concurrent users

### Stress Tests
- **BookingServiceStressTest**: Stress testing для Booking Service
  - Get user bookings
  - View booking details
  - Target: постепенное увеличение до 200+ users

## 🚀 Запуск Тестов

### Prerequisites
```bash
# Убедитесь, что все сервисы запущены
docker-compose up -d

# Или запустите отдельные сервисы:
mvn spring-boot:run -pl trip-service
mvn spring-boot:run -pl booking-service
```

### Запуск Load Tests
```bash
# Trip Service Load Test
cd performance-tests
mvn gatling:test -Dgatling.simulationClass=com.travelmaster.performance.TripServiceLoadTest
```

### Запуск Stress Tests
```bash
# Booking Service Stress Test
mvn gatling:test -Dgatling.simulationClass=com.travelmaster.performance.BookingServiceStressTest
```

### Запуск Всех Тестов
```bash
mvn gatling:test
```

## 📊 Результаты

Результаты тестов сохраняются в:
```
performance-tests/target/gatling/
└── [test-name]-[timestamp]/
    ├── index.html          # Интерактивный отчет
    ├── simulation.log      # Raw данные
    └── js/stats.json       # JSON результаты
```

### Открыть Отчет
```bash
# MacOS
open target/gatling/[test-name]-[timestamp]/index.html

# Linux
xdg-open target/gatling/[test-name]-[timestamp]/index.html
```

## 🎯 Performance SLA

### Response Time Targets
- **p95**: < 2000ms
- **p99**: < 3000ms
- **max**: < 5000ms

### Success Rate
- **Minimum**: 95%
- **Target**: 99%

### Throughput
- **Trip Service**: 10-20 req/sec
- **Booking Service**: 5-10 req/sec
- **Payment Service**: 5-10 req/sec

## 📈 Сценарии Тестирования

### 1. Load Test (TripServiceLoadTest)
**Цель**: Проверить производительность при нормальной нагрузке
- Ramp up: 50 users за 30s
- Sustain: 10 users/sec в течение 1 min
- Assertion: max response time < 3000ms, success rate > 95%

### 2. Stress Test (BookingServiceStressTest)
**Цель**: Найти breaking point системы
- Phase 1: 100 users, 20 req/sec (2 min)
- Phase 2: 200 users, 50 req/sec (2 min)
- Assertion: p99 < 5000ms, success rate > 90%

### 3. Spike Test (опционально)
**Цель**: Проверить поведение при резком росте нагрузки
- Baseline: 10 users
- Spike: 1000 users за 10s
- Sustain: 1 min
- Recovery: вернуться к baseline

### 4. Endurance Test (опционально)
**Цель**: Проверить стабильность при длительной нагрузке
- Load: 20 users/sec
- Duration: 30+ minutes
- Monitor: memory leaks, performance degradation

## 🔧 Конфигурация

### Изменить Базовый URL
```scala
val httpProtocol = http
  .baseUrl("http://your-domain.com")
```

### Изменить Нагрузку
```scala
setUp(
  scenario.inject(
    rampUsers(100) during (1.minutes),  // Больше пользователей
    constantUsersPerSec(50) during (5.minutes)  // Дольше
  )
)
```

### Изменить Assertions
```scala
.assertions(
  global.responseTime.max.lt(2000),     // Строже SLA
  global.successfulRequests.percent.gt(99)
)
```

## 📊 Метрики для Мониторинга

Во время тестов следите за:

### Application Metrics (Prometheus/Grafana)
- CPU usage
- Memory usage
- JVM heap
- Thread count
- Response times
- Request rate

### Database Metrics
- Connection pool usage
- Query execution time
- Active connections
- Slow queries

### System Metrics
- Disk I/O
- Network I/O
- Load average

## 🐛 Troubleshooting

### Тесты падают с timeout
- Увеличьте timeout в httpProtocol
- Проверьте доступность сервисов
- Уменьшите нагрузку

### High response times
- Проверьте database indexes
- Включите connection pooling
- Оптимизируйте N+1 queries
- Добавьте caching

### Out of Memory
- Увеличьте JVM heap: `-Xmx2g`
- Оптимизируйте Gatling injection profile
- Уменьшите количество concurrent users

## 🎓 Best Practices

1. **Baseline First**: Сначала установите baseline метрики
2. **Incremental**: Постепенно увеличивайте нагрузку
3. **Real Data**: Используйте реалистичные данные
4. **Monitor**: Следите за метриками во время тестов
5. **Repeat**: Повторяйте тесты для consistency
6. **Analyze**: Анализируйте bottlenecks и оптимизируйте

## 📚 Ресурсы

- [Gatling Documentation](https://gatling.io/docs/current/)
- [Performance Testing Guide](https://martinfowler.com/articles/performance-testing.html)
- [Gatling Academy](https://academy.gatling.io/)

