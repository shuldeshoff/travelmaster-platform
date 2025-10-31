package com.travelmaster.booking.saga;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Лог выполнения Saga для отслеживания и восстановления.
 * В production-системе это критически важно для диагностики и повторного выполнения.
 */
@Entity
@Table(name = "saga_logs", indexes = {
        @Index(name = "idx_saga_booking_id", columnList = "booking_id"),
        @Index(name = "idx_saga_state", columnList = "state"),
        @Index(name = "idx_saga_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SagaLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SagaState state;

    @Column(name = "step_name", length = 100)
    private String stepName;

    @Column(name = "step_description", columnDefinition = "TEXT")
    private String stepDescription;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}

