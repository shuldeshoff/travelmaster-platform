package com.travelmaster.trip.entity;

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
@Table(name = "trips", indexes = {
        @Index(name = "idx_origin_destination", columnList = "origin, destination"),
        @Index(name = "idx_departure_date", columnList = "departure_date"),
        @Index(name = "idx_price", columnList = "price")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 100)
    private String origin;

    @Column(nullable = false, length = 100)
    private String destination;

    @Column(name = "departure_date", nullable = false)
    private LocalDateTime departureDate;

    @Column(name = "return_date")
    private LocalDateTime returnDate;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(length = 3, nullable = false)
    @Builder.Default
    private String currency = "RUB";

    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    @Column(name = "available_seats", nullable = false)
    private Integer availableSeats;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TripStatus status = TripStatus.AVAILABLE;

    @Column(name = "provider_id", length = 100)
    private String providerId; // ID от внешнего провайдера (Amadeus, Booking)

    @Column(length = 50)
    private String provider; // "AMADEUS", "BOOKING", "INTERNAL"

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Segment> segments = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String inclusions; // Что включено (например, "Завтрак, Трансфер")

    @Column(columnDefinition = "TEXT")
    private String exclusions; // Что не включено

    @Column(name = "min_age")
    private Integer minAge;

    @Column(name = "max_age")
    private Integer maxAge;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    public void addSegment(Segment segment) {
        segments.add(segment);
        segment.setTrip(this);
    }

    public void removeSegment(Segment segment) {
        segments.remove(segment);
        segment.setTrip(null);
    }

    public boolean hasAvailableSeats(int requested) {
        return availableSeats != null && availableSeats >= requested;
    }

    public void reserveSeats(int count) {
        if (!hasAvailableSeats(count)) {
            throw new IllegalStateException("Not enough available seats");
        }
        this.availableSeats -= count;
    }

    public void releaseSeats(int count) {
        this.availableSeats = Math.min(this.availableSeats + count, this.totalSeats);
    }
}

