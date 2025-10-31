package com.travelmaster.booking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_trip_id", columnList = "trip_id"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_booking_reference", columnList = "booking_reference", unique = true)
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_reference", nullable = false, unique = true, length = 20)
    private String bookingReference; // Уникальный номер бронирования (например, "TM-2025-001234")

    @Column(name = "user_id", nullable = false)
    private Long userId; // Reference to User Service

    @Column(name = "trip_id", nullable = false)
    private Long tripId; // Reference to Trip Service

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;

    @Column(name = "number_of_passengers", nullable = false)
    private Integer numberOfPassengers;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(length = 3, nullable = false)
    @Builder.Default
    private String currency = "RUB";

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Passenger> passengers = new ArrayList<>();

    @Column(name = "special_requests", columnDefinition = "TEXT")
    private String specialRequests;

    // Payment information
    @Column(name = "payment_id")
    private Long paymentId; // Reference to Payment Service

    @Column(name = "paid_amount", precision = 10, scale = 2)
    private BigDecimal paidAmount;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    // Cancellation information
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    // Timestamps
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    // Business methods
    public void addPassenger(Passenger passenger) {
        passengers.add(passenger);
        passenger.setBooking(this);
    }

    public void removePassenger(Passenger passenger) {
        passengers.remove(passenger);
        passenger.setBooking(null);
    }

    public boolean canBeCancelled() {
        return status == BookingStatus.PENDING || 
               status == BookingStatus.CONFIRMED || 
               status == BookingStatus.PAID;
    }

    public boolean isPaid() {
        return status == BookingStatus.PAID || status == BookingStatus.COMPLETED;
    }

    public void markAsPaid(Long paymentId, BigDecimal amount) {
        this.paymentId = paymentId;
        this.paidAmount = amount;
        this.paidAt = LocalDateTime.now();
        this.status = BookingStatus.PAID;
    }

    public void cancel(String reason) {
        if (!canBeCancelled()) {
            throw new IllegalStateException("Booking cannot be cancelled in status: " + status);
        }
        this.status = BookingStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.cancellationReason = reason;
    }
}

