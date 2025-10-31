# ADR-002: Saga Pattern for Distributed Transactions

**Status**: Accepted  
**Date**: 2025-10-30  
**Decision Makers**: Tech Lead, Senior Backend Developer

## Context

В microservices архитектуре транзакции часто span across multiple services. Например, процесс бронирования включает:
1. Create booking (Booking Service)
2. Reserve trip seats (Trip Service)
3. Process payment (Payment Service)
4. Send notification (Notification Service)

Традиционные ACID транзакции невозможны в distributed systems. Нам нужен механизм для обеспечения data consistency across services.

## Decision

Мы реализуем **Saga Pattern** с **Orchestration-based approach** для управления distributed transactions.

### Implementation:

```java
@Service
public class BookingSagaOrchestrator {
    
    public BookingResponse executeBookingSaga(CreateBookingRequest request) {
        // 1. Create booking (PENDING)
        Booking booking = createBooking(request);
        
        try {
            // 2. Reserve trip
            reserveTrip(booking.getTripId());
            
            // 3. Process payment
            Payment payment = processPayment(booking);
            
            // 4. Confirm booking
            confirmBooking(booking.getId());
            
            // 5. Send notification
            sendNotification(booking);
            
            return toResponse(booking);
            
        } catch (Exception e) {
            // Compensating transactions
            compensate(booking);
            throw e;
        }
    }
    
    private void compensate(Booking booking) {
        // Rollback in reverse order
        cancelNotification(booking);
        refundPayment(booking);
        releaseTrip(booking.getTripId());
        cancelBooking(booking.getId());
    }
}
```

## Rationale

### Saga Pattern выбран потому что:

1. **Eventual Consistency**
   - Подходит для нашего business domain
   - Bookings могут быть в intermediate states

2. **Compensating Transactions**
   - Clear rollback mechanism
   - Business logic для отмены

3. **Reliability**
   - Retry mechanism для failed steps
   - Saga log для recovery

4. **Visibility**
   - Saga state tracked в database
   - Easy to monitor и debug

### Orchestration vs Choreography:

Мы выбрали **Orchestration** потому что:
- Central control flow (easier to understand)
- Explicit ordering of steps
- Easier testing
- Better error handling

**Choreography** (event-driven) отклонен из-за:
- Implicit flow (harder to trace)
- Cyclic dependencies risk
- Complex debugging

## Alternatives Considered

### 1. Two-Phase Commit (2PC)

**Pros**:
- Strong consistency
- ACID guarantees

**Cons**:
- Performance bottleneck (blocking)
- Single point of failure (coordinator)
- Not suitable для microservices

**Why rejected**: Не scalable, blocking nature

### 2. Event Sourcing

**Pros**:
- Complete audit trail
- Time travel debugging
- Natural fit для events

**Cons**:
- High complexity
- Steep learning curve
- Event store overhead

**Why rejected**: Overkill для нашего use case, слишком сложно

### 3. Manual Compensation

**Pros**:
- Simple to implement
- Full control

**Cons**:
- Error-prone
- No standard pattern
- Hard to maintain

**Why rejected**: Не structured, не reliable

## Consequences

### Positive:

- ✅ Reliable distributed transactions
- ✅ Clear compensation logic
- ✅ Traceable saga execution
- ✅ Retry mechanism built-in
- ✅ Testable business flows

### Negative:

- ⚠️ Eventual consistency (не immediate)
- ⚠️ Complex error handling
- ⚠️ Saga state management overhead
- ⚠️ Compensating transactions must be idempotent

### Neutral:

- 📊 Requires saga log table
- 📊 Additional monitoring needed
- 📊 Team training required

## Implementation Details

### Saga State Machine

States:
- `STARTED` - Saga initiated
- `IN_PROGRESS` - Executing steps
- `COMPLETED` - All steps successful
- `COMPENSATING` - Rolling back
- `FAILED` - Compensation failed
- `COMPENSATED` - Successfully rolled back

### Saga Log

```sql
CREATE TABLE saga_logs (
    id BIGSERIAL PRIMARY KEY,
    saga_id VARCHAR(255) NOT NULL,
    saga_type VARCHAR(50) NOT NULL,
    state VARCHAR(50) NOT NULL,
    current_step INT,
    payload JSONB,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);
```

### Retry Strategy

- Exponential backoff
- Max 3 retries
- Circuit breaker для external services

### Idempotency

All saga steps must be idempotent:
- Booking creation: check by idempotency key
- Payment processing: check transaction ID
- Notifications: deduplicate by booking ID

## Monitoring

### Metrics:
- Saga success rate
- Saga duration
- Compensation rate
- Step failure rates

### Alerts:
- High compensation rate
- Saga timeout
- Step failures > threshold

## Testing Strategy

1. **Unit Tests**: Each saga step isolated
2. **Integration Tests**: Full saga flow
3. **Chaos Testing**: Random step failures
4. **Compensation Tests**: Verify rollback logic

## References

- [Saga Pattern by Chris Richardson](https://microservices.io/patterns/data/saga.html)
- [Saga Orchestration](https://www.baeldung.com/java-saga-pattern)
- [Patterns of Distributed Transactions](https://developers.redhat.com/blog/2018/10/01/patterns-for-distributed-transactions-within-a-microservices-architecture)

---

**Last Updated**: 2025-10-31

