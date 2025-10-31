# 🔧 TravelMaster Platform - Troubleshooting Guide

Руководство по решению типичных проблем и FAQ для TravelMaster Platform.

## 📋 Содержание

1. [Общие проблемы](#общие-проблемы)
2. [Проблемы сборки](#проблемы-сборки)
3. [Проблемы с Docker](#проблемы-с-docker)
4. [Проблемы с Kubernetes](#проблемы-с-kubernetes)
5. [Проблемы с базой данных](#проблемы-с-базой-данных)
6. [Проблемы с Kafka](#проблемы-с-kafka)
7. [Проблемы производительности](#проблемы-производительности)
8. [FAQ](#faq)

---

## 🚨 Общие проблемы

### Сервис не стартует

**Симптомы**: Application fails to start, immediate exit

**Диагностика**:
```bash
# Check logs
docker-compose logs service-name

# Check Java process
ps aux | grep java

# Check port conflicts
netstat -an | grep LISTEN | grep 8080
```

**Решения**:

1. **Порт занят**:
   ```bash
   # Find process using port
   lsof -i :8080
   
   # Kill process
   kill -9 PID
   
   # Or change port
   export SERVER_PORT=8081
   ```

2. **Недостаточно памяти**:
   ```bash
   # Increase heap size
   export JAVA_OPTS="-Xmx2g -Xms512m"
   java $JAVA_OPTS -jar app.jar
   ```

3. **Проблемы с конфигурацией**:
   ```bash
   # Validate application.yml
   yamllint application.yml
   
   # Check Spring profile
   echo $SPRING_PROFILES_ACTIVE
   ```

---

### Cannot connect to database

**Симптомы**: `Connection refused`, `Unknown database`, `Access denied`

**Диагностика**:
```bash
# Check PostgreSQL is running
docker-compose ps postgres

# Test connection
psql -h localhost -U travelmaster_user -d travelmaster_user

# Check logs
docker-compose logs postgres
```

**Решения**:

1. **PostgreSQL не запущен**:
   ```bash
   docker-compose up -d postgres
   
   # Wait for ready
   docker-compose logs -f postgres | grep "ready to accept connections"
   ```

2. **Неверные credentials**:
   ```bash
   # Check environment variables
   echo $SPRING_DATASOURCE_USERNAME
   echo $SPRING_DATASOURCE_PASSWORD
   
   # Reset password
   docker-compose exec postgres psql -U postgres -c "ALTER USER travelmaster_user WITH PASSWORD 'newpassword';"
   ```

3. **Database не существует**:
   ```bash
   # Create database
   docker-compose exec postgres createdb -U postgres travelmaster_user
   
   # Verify
   docker-compose exec postgres psql -U postgres -l
   ```

---

### JWT token validation fails

**Симптомы**: `401 Unauthorized`, `Invalid JWT signature`

**Решения**:

1. **Secret key mismatch**:
   ```bash
   # Ensure JWT_SECRET is same in all services
   # Gateway and User Service must share the same secret
   
   # Check current secret
   kubectl get secret jwt-secret -n travelmaster -o yaml
   
   # Update secret
   kubectl create secret generic jwt-secret \
     --from-literal=jwt-secret='your-consistent-secret' \
     --dry-run=client -o yaml | kubectl apply -f -
   ```

2. **Token expired**:
   ```bash
   # Get new token
   curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"email":"admin@travelmaster.com","password":"admin123"}'
   ```

---

## 🔨 Проблемы сборки

### Maven build fails

**Проблема**: Compilation errors, dependency issues

**Решения**:

1. **Clean build**:
   ```bash
   mvn clean install -U
   ```

2. **Dependency conflicts**:
   ```bash
   # Show dependency tree
   mvn dependency:tree
   
   # Resolve conflicts
   mvn dependency:analyze
   ```

3. **Outdated Maven repository**:
   ```bash
   # Clear local repository
   rm -rf ~/.m2/repository
   
   # Rebuild
   mvn clean install
   ```

### Test failures

**Проблема**: Tests fail during build

**Решения**:

1. **Skip tests temporarily**:
   ```bash
   mvn clean install -DskipTests
   ```

2. **Run specific test**:
   ```bash
   mvn test -Dtest=AuthServiceTest
   ```

3. **Testcontainers issues**:
   ```bash
   # Check Docker is running
   docker ps
   
   # Increase timeout
   export TESTCONTAINERS_RYUK_DISABLED=true
   ```

---

## 🐳 Проблемы с Docker

### Container keeps restarting

**Симптомы**: Container in restart loop

**Диагностика**:
```bash
# Check status
docker-compose ps

# Check logs
docker-compose logs service-name

# Inspect container
docker inspect container-id
```

**Решения**:

1. **Health check failing**:
   ```bash
   # Disable health check temporarily
   docker-compose up -d --no-healthcheck service-name
   
   # Check health endpoint manually
   curl http://localhost:8080/actuator/health
   ```

2. **OOMKilled**:
   ```bash
   # Increase memory limit in docker-compose.yml
   mem_limit: 2g
   
   # Check memory usage
   docker stats
   ```

### Out of disk space

**Симптомы**: `no space left on device`

**Решения**:
```bash
# Clean up Docker
docker system prune -a --volumes

# Remove unused images
docker image prune -a

# Check disk usage
docker system df
```

### Network issues

**Проблема**: Services can't communicate

**Решения**:
```bash
# Recreate network
docker-compose down
docker network prune
docker-compose up -d

# Check network
docker network ls
docker network inspect travelmaster_default

# Test connectivity
docker-compose exec gateway-service ping user-service
```

---

## ☸️ Проблемы с Kubernetes

### Pods not starting

**Симптомы**: Pods in `Pending`, `CrashLoopBackOff`, or `ImagePullBackOff` state

**Диагностика**:
```bash
# Check pod status
kubectl get pods -n travelmaster

# Describe pod
kubectl describe pod pod-name -n travelmaster

# Check events
kubectl get events -n travelmaster --sort-by='.lastTimestamp'
```

**Решения**:

1. **ImagePullBackOff**:
   ```bash
   # Verify image exists
   docker images | grep travelmaster
   
   # Push to registry
   docker push your-registry/travelmaster/gateway:latest
   
   # Update deployment
   kubectl set image deployment/gateway-deployment \
     gateway=your-registry/travelmaster/gateway:latest \
     -n travelmaster
   ```

2. **CrashLoopBackOff**:
   ```bash
   # Check logs
   kubectl logs pod-name -n travelmaster --previous
   
   # Common causes:
   # - Missing ConfigMap/Secret
   # - Database not ready
   # - Wrong environment variables
   ```

3. **Insufficient resources**:
   ```bash
   # Check node resources
   kubectl top nodes
   
   # Adjust resource requests
   kubectl edit deployment gateway-deployment -n travelmaster
   ```

### Service not accessible

**Проблема**: Cannot access service from outside cluster

**Решения**:

1. **Port forwarding**:
   ```bash
   # Forward local port to service
   kubectl port-forward -n travelmaster svc/gateway-service 8080:8080
   
   # Test
   curl http://localhost:8080/actuator/health
   ```

2. **Ingress issues**:
   ```bash
   # Check ingress
   kubectl get ingress -n travelmaster
   kubectl describe ingress travelmaster-ingress -n travelmaster
   
   # Check ingress controller
   kubectl get pods -n ingress-nginx
   ```

3. **Service selector mismatch**:
   ```bash
   # Check service selector
   kubectl get svc gateway-service -n travelmaster -o yaml
   
   # Check pod labels
   kubectl get pods -n travelmaster --show-labels
   ```

### ConfigMap/Secret not found

**Проблема**: Pod fails with "configmap not found"

**Решения**:
```bash
# List ConfigMaps
kubectl get configmap -n travelmaster

# Create missing ConfigMap
kubectl apply -f k8s/base/configmaps/ -n travelmaster

# Restart pods to pick up changes
kubectl rollout restart deployment/gateway-deployment -n travelmaster
```

---

## 🗄️ Проблемы с базой данных

### Flyway migration fails

**Симптомы**: `FlywayException`, `Validate failed`

**Решения**:

1. **Baseline existing database**:
   ```bash
   # Connect to database
   psql -h localhost -U travelmaster_user -d travelmaster_user
   
   # Check flyway_schema_history
   SELECT * FROM flyway_schema_history;
   
   # Repair if needed
   mvn flyway:repair
   ```

2. **Migration conflict**:
   ```bash
   # Force version
   mvn flyway:baseline -Dflyway.baselineVersion=1
   
   # Or clean and migrate (DANGEROUS!)
   mvn flyway:clean flyway:migrate
   ```

### Connection pool exhausted

**Симптомы**: `Unable to acquire JDBC Connection`, timeout errors

**Решения**:
```yaml
# Adjust connection pool in application.yml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

### Slow queries

**Диагностика**:
```sql
-- Enable query logging
ALTER DATABASE travelmaster_user SET log_statement = 'all';

-- Check slow queries
SELECT pid, now() - query_start AS duration, query
FROM pg_stat_activity
WHERE state = 'active' AND now() - query_start > interval '5 seconds';

-- Check missing indexes
SELECT schemaname, tablename, attname, n_distinct, correlation
FROM pg_stats
WHERE schemaname = 'public'
ORDER BY abs(correlation) DESC;
```

**Решения**:
```sql
-- Add index
CREATE INDEX idx_bookings_user_id ON bookings(user_id);

-- Analyze table
ANALYZE bookings;

-- Vacuum
VACUUM ANALYZE;
```

---

## 📨 Проблемы с Kafka

### Cannot connect to Kafka

**Симптомы**: `Connection refused`, `TimeoutException`

**Диагностика**:
```bash
# Check Kafka is running
docker-compose ps kafka zookeeper

# Check Kafka logs
docker-compose logs kafka

# Test connection
docker-compose exec kafka kafka-topics.sh --list --bootstrap-server localhost:9092
```

**Решения**:

1. **Zookeeper not ready**:
   ```bash
   # Ensure Zookeeper starts first
   docker-compose up -d zookeeper
   sleep 10
   docker-compose up -d kafka
   ```

2. **Wrong bootstrap servers**:
   ```yaml
   # In application.yml
   spring:
     kafka:
       bootstrap-servers: kafka-service:9092  # Use service name in K8s/Docker
       # or
       bootstrap-servers: localhost:9092      # For local development
   ```

### Messages not consumed

**Проблема**: Producer sends but consumer doesn't receive

**Диагностика**:
```bash
# List topics
docker-compose exec kafka kafka-topics.sh --list --bootstrap-server localhost:9092

# Describe consumer group
docker-compose exec kafka kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --describe --group booking-service-group

# Check lag
docker-compose exec kafka kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --describe --group booking-service-group \
  | grep LAG
```

**Решения**:

1. **Consumer not subscribed**:
   ```java
   // Ensure @KafkaListener is properly configured
   @KafkaListener(topics = "booking-events", groupId = "booking-service-group")
   public void handleBookingEvent(BookingCreatedEvent event) {
       // ...
   }
   ```

2. **Deserialization error**:
   ```yaml
   # Add error handler
   spring:
     kafka:
       consumer:
         key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
         value-deserializer: org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
         properties:
           spring.deserializer.value.delegate.class: org.springframework.kafka.support.serializer.JsonDeserializer
   ```

---

## ⚡ Проблемы производительности

### High CPU usage

**Диагностика**:
```bash
# Docker
docker stats

# Kubernetes
kubectl top pods -n travelmaster

# JVM
jstack <pid>
```

**Решения**:

1. **Increase resources**:
   ```yaml
   # docker-compose.yml
   resources:
     limits:
       cpus: '2.0'
       memory: 2G
   ```

2. **Optimize code**:
   - Check for infinite loops
   - Review database queries
   - Add caching
   - Optimize algorithms

### High memory usage

**Диагностика**:
```bash
# Check heap usage
jmap -heap <pid>

# Dump heap
jmap -dump:format=b,file=heap.bin <pid>

# Analyze with VisualVM or Eclipse MAT
```

**Решения**:

1. **Increase heap**:
   ```bash
   export JAVA_OPTS="-Xmx4g -Xms1g"
   ```

2. **Find memory leaks**:
   - Use profiler
   - Check for unclosed resources
   - Review cache sizes

### Slow response times

**Диагностика**:
```bash
# Check endpoint metrics
curl http://localhost:8080/actuator/metrics/http.server.requests

# Distributed tracing
# Open Zipkin: http://localhost:9411
```

**Решения**:

1. **Add caching**:
   ```java
   @Cacheable("trips")
   public TripResponse getTrip(Long id) {
       // ...
   }
   ```

2. **Optimize queries**:
   - Add indexes
   - Use pagination
   - Reduce N+1 queries

3. **Scale horizontally**:
   ```bash
   # Kubernetes
   kubectl scale deployment gateway-deployment --replicas=3 -n travelmaster
   ```

---

## ❓ FAQ

### Q: Как изменить порт сервиса?

**A**: Установите переменную окружения:
```bash
export SERVER_PORT=8081
# or in application.yml
server:
  port: 8081
```

### Q: Как сбросить пароль admin?

**A**: 
```sql
-- Connect to database
psql -h localhost -U travelmaster_user -d travelmaster_user

-- Update password (BCrypt hash for "newpassword")
UPDATE users SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy' WHERE email = 'admin@travelmaster.com';
```

### Q: Как очистить все данные?

**A**: 
```bash
# Docker Compose
docker-compose down -v

# Kubernetes
kubectl delete namespace travelmaster
```

### Q: Как обновить зависимости?

**A**:
```bash
# Check for updates
mvn versions:display-dependency-updates

# Update versions in pom.xml
# Then rebuild
mvn clean install
```

### Q: Как включить debug logging?

**A**:
```yaml
# application.yml
logging:
  level:
    root: DEBUG
    com.travelmaster: TRACE
```

### Q: Как проверить версию API?

**A**:
```bash
curl http://localhost:8080/actuator/info
```

### Q: Как сделать backup базы данных?

**A**:
```bash
# Dump all databases
docker-compose exec postgres pg_dumpall -U postgres > backup.sql

# Dump specific database
docker-compose exec postgres pg_dump -U travelmaster_user travelmaster_user > user_backup.sql

# Restore
docker-compose exec -T postgres psql -U postgres < backup.sql
```

### Q: Как мониторить производительность?

**A**: Используйте встроенные инструменты:
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000
- Zipkin: http://localhost:9411
- Spring Actuator: http://localhost:8080/actuator

---

## 🆘 Дополнительная помощь

Если проблема не решена:

1. **Проверьте логи**:
   ```bash
   # Docker
   docker-compose logs -f service-name
   
   # Kubernetes
   kubectl logs -f pod-name -n travelmaster
   ```

2. **Проверьте health**:
   ```bash
   curl http://localhost:8080/actuator/health
   ```

3. **Создайте issue**:
   - [GitHub Issues](https://github.com/your-org/travelmaster-platform/issues)
   - Приложите логи
   - Опишите шаги воспроизведения

4. **Контакты**:
   - Email: support@travelmaster.com
   - Slack: #travelmaster-support

---

## 📚 Дополнительные ресурсы

- [Deployment Guide](DEPLOYMENT.md)
- [Architecture Documentation](ARCHITECTURE.md)
- [Security Guide](SECURITY.md)
- [API Documentation](API_SPEC.yaml)

---

**Версия**: 1.0  
**Последнее обновление**: 31 октября 2025

