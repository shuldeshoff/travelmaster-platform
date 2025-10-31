# Руководство для команды TravelMaster

Добро пожаловать в команду разработки TravelMaster Platform! Этот документ описывает наши процессы, стандарты и лучшие практики.

## Содержание

1. [Git Workflow](#git-workflow)
2. [Code Standards](#code-standards)
3. [Code Review](#code-review)
4. [Testing](#testing)
5. [CI/CD Process](#cicd-process)
6. [Development Environment](#development-environment)
7. [Communication](#communication)

---

## Git Workflow

Мы используем **Git Flow** с некоторыми модификациями.

### Основные ветки

- `main` — production-ready код, защищена от прямых коммитов
- `develop` — интеграционная ветка для разработки

### Вспомогательные ветки

#### Feature branches

Для новой функциональности:

```bash
git checkout -b feature/TM-123-add-user-authentication develop
```

Naming convention: `feature/TICKET-ID-short-description`

#### Bugfix branches

Для исправления багов:

```bash
git checkout -b bugfix/TM-456-fix-payment-timeout develop
```

Naming convention: `bugfix/TICKET-ID-short-description`

#### Hotfix branches

Для критических исправлений в production:

```bash
git checkout -b hotfix/TM-789-critical-security-fix main
```

Naming convention: `hotfix/TICKET-ID-short-description`

### Workflow процесс

1. **Создание ветки**
   ```bash
   git checkout develop
   git pull origin develop
   git checkout -b feature/TM-123-your-feature
   ```

2. **Разработка**
   - Делайте атомарные коммиты
   - Пишите понятные commit messages
   - Регулярно синхронизируйтесь с develop

3. **Синхронизация**
   ```bash
   git fetch origin
   git rebase origin/develop
   ```

4. **Push и Pull Request**
   ```bash
   git push origin feature/TM-123-your-feature
   ```
   Создайте Pull Request в GitHub

5. **Code Review**
   - Минимум 2 апрува от других разработчиков
   - Все комментарии должны быть разрешены
   - CI pipeline должен быть зелёным

6. **Merge**
   ```bash
   # Squash merge для feature веток
   git merge --squash feature/TM-123-your-feature
   ```

### Commit Messages

Используем [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types:**
- `feat`: Новая функциональность
- `fix`: Исправление бага
- `docs`: Изменения в документации
- `style`: Форматирование, отсутствующие точки с запятой и т.д.
- `refactor`: Рефакторинг кода
- `test`: Добавление или исправление тестов
- `chore`: Изменения в процессе сборки или вспомогательных инструментах

**Примеры:**

```
feat(user-service): add OAuth2 authentication

Implement OAuth2 authentication flow with Google and GitHub providers.
- Add OAuth2 configuration
- Create OAuth2 user service
- Add integration tests

Closes #123
```

```
fix(payment-service): prevent duplicate payment processing

Add idempotency key validation to prevent duplicate charges.

Fixes #456
```

---

## Code Standards

### Java Code Style

Мы следуем [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html) с небольшими модификациями.

#### Основные правила

**1. Naming Conventions**

```java
// Classes: PascalCase
public class UserService { }

// Methods: camelCase
public void processPayment() { }

// Constants: UPPER_SNAKE_CASE
public static final int MAX_RETRY_ATTEMPTS = 3;

// Variables: camelCase
private String userName;
```

**2. Formatting**

- **Indentation:** 4 spaces (не tabs)
- **Line length:** 120 символов
- **Braces:** K&R style

```java
public class Example {
    
    public void method() {
        if (condition) {
            // code
        } else {
            // code
        }
    }
}
```

**3. Imports**

- Никаких wildcard imports
- Группировка: java.*, javax.*, org.*, com.*
- Alphabetical order внутри групп

```java
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.travelmaster.common.dto.UserDto;
```

**4. JavaDoc**

Обязателен для:
- Public classes
- Public methods
- Complex private methods

```java
/**
 * Processes payment for a booking.
 *
 * @param bookingId the booking identifier
 * @param amount the payment amount
 * @return payment result with transaction ID
 * @throws PaymentException if payment processing fails
 */
public PaymentResult processPayment(Long bookingId, BigDecimal amount) {
    // implementation
}
```

### Spring Boot Best Practices

**1. Dependency Injection**

Используйте constructor injection:

```java
@Service
@RequiredArgsConstructor // Lombok
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    // methods
}
```

**2. Configuration Properties**

```java
@ConfigurationProperties(prefix = "app.payment")
@Validated
public class PaymentProperties {
    
    @NotBlank
    private String apiKey;
    
    @Min(1)
    @Max(10)
    private int maxRetryAttempts = 3;
    
    // getters/setters
}
```

**3. Exception Handling**

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(ex.getMessage()));
    }
}
```

**4. Validation**

```java
@PostMapping("/users")
public ResponseEntity<UserDto> createUser(
        @Valid @RequestBody CreateUserRequest request) {
    // implementation
}
```

### Database

**1. Naming Conventions**

- Tables: `snake_case` (e.g., `user_profiles`)
- Columns: `snake_case` (e.g., `first_name`)
- Indexes: `idx_table_column`
- Foreign keys: `fk_table1_table2`

**2. Migrations**

Используем Flyway с versioned migrations:

```
V1__initial_schema.sql
V2__add_user_roles.sql
V3__add_payment_audit_log.sql
```

---

## Code Review

### Checklist для автора

Перед созданием PR убедитесь:

- [ ] Код компилируется без warnings
- [ ] Все тесты проходят (`mvn verify`)
- [ ] Code coverage не упал
- [ ] Нет linter ошибок
- [ ] JavaDoc добавлен для public API
- [ ] README/документация обновлена (если нужно)
- [ ] Нет TODO/FIXME комментариев
- [ ] Секреты не закоммичены
- [ ] Миграции БД добавлены (если нужно)

### Checklist для ревьюера

При ревью обратите внимание на:

- [ ] **Функциональность:** код делает то, что должен
- [ ] **Читаемость:** код понятен и хорошо структурирован
- [ ] **Тесты:** достаточное покрытие тестами
- [ ] **Performance:** нет очевидных проблем с производительностью
- [ ] **Security:** нет уязвимостей безопасности
- [ ] **Error handling:** корректная обработка ошибок
- [ ] **Logging:** достаточно логов для debugging
- [ ] **Documentation:** код и API задокументированы
- [ ] **Standards:** соответствие code standards

### Комментарии в PR

Используйте префиксы:

- **MUST:** критическое изменение, блокирует merge
- **SHOULD:** рекомендация, желательно исправить
- **COULD:** опциональное улучшение
- **QUESTION:** вопрос для обсуждения
- **PRAISE:** похвала хорошего кода

Пример:

```
MUST: This method has a SQL injection vulnerability. 
Use PreparedStatement instead of string concatenation.

SHOULD: Consider extracting this logic into a separate method 
for better testability.

PRAISE: Great use of Optional here!
```

---

## Testing

### Типы тестов

#### 1. Unit Tests

**Цель:** Тестировать изолированные компоненты

**Инструменты:** JUnit 5, Mockito, AssertJ

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    void shouldCreateUser() {
        // given
        CreateUserRequest request = new CreateUserRequest("john@example.com");
        User user = new User(1L, "john@example.com");
        when(userRepository.save(any())).thenReturn(user);
        
        // when
        UserDto result = userService.createUser(request);
        
        // then
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        verify(userRepository).save(any(User.class));
    }
}
```

#### 2. Integration Tests

**Цель:** Тестировать взаимодействие компонентов

**Инструменты:** Spring Boot Test, Testcontainers

```java
@SpringBootTest
@Testcontainers
class UserServiceIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14");
    
    @Autowired
    private UserService userService;
    
    @Test
    void shouldSaveAndRetrieveUser() {
        // given
        CreateUserRequest request = new CreateUserRequest("john@example.com");
        
        // when
        UserDto created = userService.createUser(request);
        UserDto retrieved = userService.getUser(created.getId());
        
        // then
        assertThat(retrieved.getEmail()).isEqualTo("john@example.com");
    }
}
```

#### 3. E2E Tests

**Цель:** Тестировать полный user flow

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class BookingE2ETest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void shouldCompleteBookingFlow() {
        // 1. Create user
        // 2. Search trips
        // 3. Create booking
        // 4. Process payment
        // 5. Verify notification sent
    }
}
```

### Test Coverage

**Минимальные требования:**
- Unit tests: ≥ 80%
- Integration tests: ≥ 70%
- Critical paths: 100%

Проверка coverage:

```bash
mvn jacoco:report
open target/site/jacoco/index.html
```

---

## CI/CD Process

### CI Pipeline (на каждый PR)

1. **Build** — компиляция кода
2. **Unit Tests** — быстрые unit тесты
3. **Integration Tests** — тесты с Testcontainers
4. **Code Quality** — SonarQube анализ
5. **Security Scan** — OWASP dependency check
6. **Docker Build** — сборка образов (только для main/develop)

### CD Pipeline (на merge в main/develop)

1. **Build & Test** — полный CI pipeline
2. **Docker Push** — публикация образов в registry
3. **Deploy to Staging** — автоматический деплой
4. **Smoke Tests** — базовые проверки
5. **Deploy to Production** — ручной approval

### Deployment процесс

**Staging:**
```bash
# Автоматически при merge в develop
git push origin develop
```

**Production:**
```bash
# Вручную через GitHub Actions
# Actions → CD Pipeline → Run workflow → Select 'production'
```

---

## Development Environment

### Требования

- **Java:** 21 (Temurin или Oracle JDK)
- **Maven:** 3.8+
- **Docker:** 24.x
- **IDE:** IntelliJ IDEA (рекомендуется) или Eclipse

### Первоначальная настройка

1. **Клонирование репозитория**
   ```bash
   git clone https://github.com/shuldeshoff/travelmaster-platform.git
   cd travelmaster-platform
   ```

2. **Запуск инфраструктуры**
   ```bash
   docker-compose up -d
   ```

3. **Сборка проекта**
   ```bash
   mvn clean install
   ```

4. **Запуск сервиса**
   ```bash
   cd user-service
   mvn spring-boot:run
   ```

### IDE Setup

#### IntelliJ IDEA

1. **Import Project**
   - File → Open → Select `pom.xml`

2. **Install Plugins**
   - Lombok Plugin
   - SonarLint
   - CheckStyle-IDEA

3. **Code Style**
   - Import `config/intellij-java-style.xml`
   - Settings → Editor → Code Style → Import Scheme

4. **Run Configuration**
   - Create Spring Boot run config for each service
   - Set working directory and environment variables

### Useful Commands

```bash
# Build all modules
mvn clean install

# Run specific service
mvn spring-boot:run -pl user-service

# Run tests
mvn test

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Skip tests
mvn clean install -DskipTests

# Generate coverage report
mvn jacoco:report

# Check dependencies for vulnerabilities
mvn dependency-check:check

# Update dependencies
mvn versions:display-dependency-updates
```

---

## Communication

### Каналы коммуникации

- **Slack:**
  - `#travelmaster-dev` — общие вопросы разработки
  - `#travelmaster-alerts` — CI/CD и production alerts
  - `#travelmaster-random` — неформальное общение

- **Jira:** Управление задачами и спринтами
- **Confluence:** Документация и дизайн-доки
- **GitHub:** Code review и technical discussions

### Daily Standup

**Время:** 10:00 каждый день (15 минут max)

**Формат:**
1. Что сделал вчера?
2. Что планирую сегодня?
3. Есть ли блокеры?

### Sprint Planning

**Когда:** Каждый понедельник, начало спринта (2 недели)

**Цель:** Планирование работы на спринт

### Retrospective

**Когда:** Каждую вторую пятницу, конец спринта

**Формат:**
- Что прошло хорошо?
- Что можно улучшить?
- Action items

---

## Resources

### Documentation
- [Architecture Overview](ARCHITECTURE.md)
- [Security Guidelines](SECURITY.md)
- [API Specification](API_SPEC.yaml)

### External
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/)
- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)

### Internal
- Wiki: https://wiki.travelmaster.com
- API Docs: https://api.travelmaster.com/docs
- Monitoring: https://monitoring.travelmaster.com

---

## Getting Help

- **Technical questions:** Ask in `#travelmaster-dev`
- **Urgent production issues:** Ping `@oncall-engineer`
- **HR/Administrative:** Contact your manager

---

**Welcome aboard!** 🚀

Если у вас есть вопросы или предложения по улучшению этого руководства, создайте PR или обсудите в `#travelmaster-dev`.

