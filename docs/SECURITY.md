# Безопасность TravelMaster Platform

Этот документ описывает меры безопасности, применяемые в TravelMaster Platform, и наше соответствие стандартам защиты данных.

## Содержание

1. [Security Overview](#security-overview)
2. [Аутентификация и авторизация](#аутентификация-и-авторизация)
3. [Защита данных](#защита-данных)
4. [Compliance](#compliance)
5. [Security Best Practices](#security-best-practices)
6. [Incident Response](#incident-response)
7. [Vulnerability Reporting](#vulnerability-reporting)

---

## Security Overview

TravelMaster Platform реализует многоуровневую защиту безопасности:

- **Аутентификация:** JWT + OAuth 2.0
- **Авторизация:** RBAC (Role-Based Access Control)
- **Шифрование в transit:** TLS 1.3
- **Шифрование at rest:** AES-256
- **Network security:** Kubernetes Network Policies
- **Audit logging:** Все действия с персональными данными логируются

---

## Аутентификация и авторизация

### JWT Authentication

Мы используем JWT (JSON Web Tokens) для stateless аутентификации.

#### Token Structure

```json
{
  "header": {
    "alg": "RS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "user-id",
    "email": "user@example.com",
    "roles": ["TRAVELER"],
    "iat": 1635724800,
    "exp": 1635811200
  }
}
```

#### Token Lifecycle

1. **Login:** User → credentials → JWT token (15 min expiry)
2. **Access:** Client → JWT in Authorization header → Protected resource
3. **Refresh:** Refresh token (30 days) → New JWT token
4. **Logout:** Token revocation (Redis blacklist)

```bash
# Request example
curl -H "Authorization: Bearer eyJhbGciOiJS..." \
  https://api.travelmaster.com/api/bookings
```

#### Token Storage

- **Backend:** Signed with RS256 (private key)
- **Client:** Stored in httpOnly secure cookies or localStorage
- **Revocation:** Redis blacklist for logged out tokens

### OAuth 2.0 / OpenID Connect

Поддержка социальной аутентификации:

- Google
- GitHub
- Facebook (планируется)
- Apple (планируется)

```yaml
# Configuration
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope: openid,profile,email
```

### Role-Based Access Control (RBAC)

#### Роли

- **TRAVELER** — обычный пользователь
- **AGENT** — турагент с расширенными правами
- **ADMIN** — администратор системы

#### Permissions

| Resource | TRAVELER | AGENT | ADMIN |
|----------|----------|-------|-------|
| View trips | ✅ | ✅ | ✅ |
| Create booking | ✅ | ✅ | ✅ |
| Cancel any booking | ❌ | ✅ | ✅ |
| Manage users | ❌ | ❌ | ✅ |
| View analytics | ❌ | ✅ | ✅ |
| System config | ❌ | ❌ | ✅ |

#### Implementation

```java
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/users")
public List<UserDto> getAllUsers() {
    return userService.getAllUsers();
}

@PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
@GetMapping("/analytics")
public AnalyticsDto getAnalytics() {
    return analyticsService.getAnalytics();
}
```

---

## Защита данных

### Шифрование в Transit

**TLS 1.3** для всех соединений:

- Client ↔ Gateway: TLS 1.3
- Service ↔ Service: mTLS (mutual TLS) в Kubernetes
- Service ↔ Database: TLS/SSL

```yaml
# Kubernetes ingress with TLS
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: travelmaster-ingress
  annotations:
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
spec:
  tls:
    - hosts:
        - api.travelmaster.com
      secretName: travelmaster-tls
```

### Шифрование at Rest

#### Database

Шифрование чувствительных полей:

```java
@Entity
public class User {
    
    @Id
    private Long id;
    
    // Открытые данные
    private String email;
    
    // Зашифрованные поля
    @Convert(converter = EncryptedStringConverter.class)
    private String passportNumber;
    
    @Convert(converter = EncryptedStringConverter.class)
    private String phoneNumber;
}
```

#### Secrets Management

- **Kubernetes Secrets:** Для credentials и API keys
- **External Secrets Operator:** Интеграция с Vault (планируется)
- **Environment variables:** Никогда не хардкодим секреты

```yaml
# Kubernetes Secret
apiVersion: v1
kind: Secret
metadata:
  name: db-credentials
type: Opaque
data:
  username: dXNlcm5hbWU=  # base64 encoded
  password: cGFzc3dvcmQ=  # base64 encoded
```

### Токенизация платёжных данных

**PCI DSS compliance:** Платёжные данные не хранятся в нашей БД.

Вместо хранения карточных данных:

1. Client отправляет данные карты → Payment Gateway
2. Payment Gateway → Возвращает токен
3. Мы храним только токен

```java
@Entity
public class PaymentMethod {
    @Id
    private Long id;
    
    private String tokenizedCardNumber;  // "tok_1234567890"
    private String lastFourDigits;        // "4242"
    private String cardBrand;             // "VISA"
    
    // НЕТ полного номера карты!
}
```

### Маскирование данных в логах

```java
@Slf4j
public class UserService {
    
    public void processPayment(PaymentRequest request) {
        // ❌ ПЛОХО
        log.info("Processing payment for card: {}", request.getCardNumber());
        
        // ✅ ХОРОШО
        log.info("Processing payment for card ending in: {}", 
            maskCardNumber(request.getCardNumber()));
    }
    
    private String maskCardNumber(String cardNumber) {
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
}
```

---

## Compliance

### ФЗ-152 (Федеральный закон о персональных данных)

**Требования:**
- Согласие на обработку персональных данных
- Право на доступ к своим данным
- Право на удаление данных (забвение)
- Защита при хранении и передаче
- Уведомление при утечке

**Реализация:**

#### 1. Согласие на обработку

```java
@Entity
public class UserConsent {
    @Id
    private Long id;
    
    @ManyToOne
    private User user;
    
    private ConsentType type;  // PERSONAL_DATA, MARKETING, etc.
    private boolean granted;
    private LocalDateTime grantedAt;
    private String ipAddress;
}
```

#### 2. Экспорт данных

```java
@GetMapping("/me/export")
public ResponseEntity<byte[]> exportMyData() {
    User user = getCurrentUser();
    PersonalDataExport export = dataExportService.exportUserData(user);
    return ResponseEntity.ok()
        .header("Content-Disposition", "attachment; filename=my-data.json")
        .body(export.toJson());
}
```

#### 3. Удаление данных (Right to be Forgotten)

```java
@DeleteMapping("/me")
public ResponseEntity<Void> deleteMyAccount() {
    User user = getCurrentUser();
    
    // Анонимизация вместо удаления (для сохранения целостности)
    userService.anonymizeUser(user);
    
    return ResponseEntity.noContent().build();
}

public void anonymizeUser(User user) {
    user.setEmail("deleted-" + user.getId() + "@anonymized.com");
    user.setFirstName("DELETED");
    user.setLastName("USER");
    user.setPhoneNumber(null);
    user.setPassportNumber(null);
    user.setDeleted(true);
    user.setDeletedAt(LocalDateTime.now());
}
```

#### 4. Audit Logging

Все операции с персональными данными логируются:

```java
@Entity
public class AuditLog {
    @Id
    private Long id;
    
    private Long userId;
    private String action;  // "READ", "UPDATE", "DELETE"
    private String entity;  // "USER", "BOOKING"
    private String details;
    private String ipAddress;
    private LocalDateTime timestamp;
}
```

### PCI DSS (Payment Card Industry Data Security Standard)

**Требования:**
- Не хранить полные номера карт
- Не хранить CVV/CVC
- Токенизация платёжных данных
- Шифрование при передаче
- Ограниченный доступ к платёжным данным
- Регулярные security audits

**Реализация:**

#### 1. Токенизация

```java
public PaymentToken tokenizeCard(CardDetails card) {
    // Отправка в payment gateway
    TokenResponse response = paymentGateway.tokenize(card);
    
    // Сохраняем только токен
    PaymentMethod method = new PaymentMethod();
    method.setToken(response.getToken());
    method.setLastFourDigits(card.getNumber().substring(12));
    method.setCardBrand(detectCardBrand(card.getNumber()));
    
    return paymentMethodRepository.save(method);
}
```

#### 2. Ограниченный доступ

```java
@PreAuthorize("hasRole('PAYMENT_ADMIN')")
@GetMapping("/payments/sensitive")
public List<PaymentDto> getSensitivePaymentData() {
    // Только специальные администраторы
}
```

#### 3. Audit Trail

Все платёжные операции логируются:

```java
@Aspect
@Component
public class PaymentAuditAspect {
    
    @After("@annotation(AuditPayment)")
    public void auditPayment(JoinPoint joinPoint) {
        // Log payment operation
        auditService.logPaymentOperation(
            joinPoint.getSignature().getName(),
            joinPoint.getArgs()
        );
    }
}
```

### GDPR (General Data Protection Regulation)

Хотя TravelMaster — российская платформа, мы также соблюдаем GDPR для европейских пользователей:

- **Right to Access:** API для экспорта данных
- **Right to Erasure:** Анонимизация/удаление
- **Right to Rectification:** Возможность исправления данных
- **Data Portability:** Экспорт в машиночитаемом формате (JSON)
- **Privacy by Design:** Безопасность с момента проектирования

---

## Security Best Practices

### 1. OWASP Top 10 Protection

#### A01: Broken Access Control

```java
// ✅ Всегда проверяем ownership
@GetMapping("/bookings/{id}")
public BookingDto getBooking(@PathVariable Long id) {
    Booking booking = bookingService.findById(id);
    
    if (!booking.getUserId().equals(getCurrentUserId())) {
        throw new AccessDeniedException("Not your booking");
    }
    
    return bookingMapper.toDto(booking);
}
```

#### A02: Cryptographic Failures

```java
// ✅ Используем современные алгоритмы
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);  // Strong cost factor
}
```

#### A03: Injection

```java
// ✅ Используем PreparedStatement / JPA
@Query("SELECT u FROM User u WHERE u.email = :email")
User findByEmail(@Param("email") String email);

// ❌ НИКОГДА не делайте так
// String sql = "SELECT * FROM users WHERE email = '" + email + "'";
```

#### A04: Insecure Design

- Design reviews перед реализацией
- Threat modeling для новых фич
- Security testing в CI/CD

#### A05: Security Misconfiguration

```yaml
# ✅ Production configuration
spring:
  devtools:
    enabled: false
  
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
        # НЕ expose все endpoints
```

#### A07: XSS (Cross-Site Scripting)

```java
// ✅ Spring автоматически экранирует
@GetMapping("/users/{id}")
public String getUserProfile(@PathVariable Long id, Model model) {
    User user = userService.findById(id);
    model.addAttribute("user", user);
    return "user-profile";  // Thymeleaf автоматически экранирует
}
```

### 2. API Security

#### Rate Limiting

```yaml
# Gateway configuration
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter:
                  replenishRate: 10
                  burstCapacity: 20
```

#### CORS Configuration

```java
@Configuration
public class CorsConfig {
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("https://app.travelmaster.com"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
```

#### CSRF Protection

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf()
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .and()
            .authorizeHttpRequests()
                .anyRequest().authenticated();
        
        return http.build();
    }
}
```

### 3. Dependency Security

```bash
# Регулярные проверки зависимостей
mvn org.owasp:dependency-check-maven:check

# Автоматические обновления через Dependabot
# .github/dependabot.yml
```

### 4. Secrets Management

```bash
# ❌ НИКОГДА не коммитьте секреты
git secrets --scan

# ✅ Используйте переменные окружения
export DATABASE_PASSWORD="..."

# ✅ Kubernetes Secrets
kubectl create secret generic db-creds --from-literal=password=...
```

---

## Incident Response

### Security Incident Response Plan

#### 1. Detection & Analysis

- Мониторинг security logs
- Alerts от security tools
- User reports

#### 2. Containment

- Изоляция скомпрометированных систем
- Блокировка атакующих IP
- Revoke скомпрометированных tokens

#### 3. Eradication

- Устранение уязвимости
- Патчинг систем
- Смена credentials

#### 4. Recovery

- Восстановление сервисов
- Верификация integrity
- Мониторинг на повторные атаки

#### 5. Post-Incident

- Post-mortem analysis
- Документирование урока
- Обновление security policies

### Emergency Contacts

- **Security Team:** security@travelmaster.com
- **On-call Engineer:** +7-XXX-XXX-XXXX
- **Management:** escalation@travelmaster.com

---

## Vulnerability Reporting

### Responsible Disclosure

Если вы обнаружили уязвимость:

1. **НЕ публикуйте её публично**
2. **Отправьте отчёт:** security@travelmaster.com
3. **Дайте нам 90 дней** на исправление
4. **Получите благодарность** в Hall of Fame

### Bug Bounty Program (планируется)

Мы планируем запустить Bug Bounty программу с вознаграждениями за найденные уязвимости.

### Report Format

```
Subject: [SECURITY] Brief description

Description:
Detailed description of the vulnerability

Steps to Reproduce:
1. Step one
2. Step two
3. ...

Impact:
What can an attacker do?

Suggested Fix:
(optional)

Your Contact:
Email for follow-up
```

---

## Security Checklist для разработчиков

- [ ] Все входные данные валидируются
- [ ] Используется parameterized queries (не string concatenation)
- [ ] Passwords хэшируются (bcrypt/argon2)
- [ ] Секреты не хардкодятся
- [ ] HTTPS для всех соединений
- [ ] JWT tokens имеют expiration
- [ ] Authorization проверяется на каждом endpoint
- [ ] Чувствительные данные маскируются в логах
- [ ] Error messages не раскрывают внутреннюю информацию
- [ ] Dependencies регулярно обновляются
- [ ] Security testing в CI/CD

---

## Resources

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [PCI DSS Requirements](https://www.pcisecuritystandards.org/)
- [ФЗ-152 О персональных данных](http://www.consultant.ru/document/cons_doc_LAW_61801/)
- [Spring Security Documentation](https://docs.spring.io/spring-security/)

---

**Version:** 1.0  
**Last Updated:** 31 октября 2025  
**Next Review:** Январь 2026

**Security is everyone's responsibility!** 🔒

