package com.travelmaster.trip.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Сегмент поездки - может быть перелёт, отель или трансфер.
 */
@Entity
@Table(name = "segments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Segment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SegmentType type;

    @Column(name = "segment_order", nullable = false)
    private Integer order; // Порядок сегмента в поездке

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    // Общие поля
    @Column(length = 100)
    private String provider; // Провайдер этого сегмента

    @Column(name = "provider_reference", length = 100)
    private String providerReference;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Для перелётов
    @Column(name = "flight_number", length = 20)
    private String flightNumber;

    @Column(name = "airline", length = 100)
    private String airline;

    @Column(name = "departure_airport", length = 10)
    private String departureAirport;

    @Column(name = "arrival_airport", length = 10)
    private String arrivalAirport;

    @Column(name = "cabin_class", length = 50)
    private String cabinClass;

    // Для отелей
    @Column(name = "hotel_name", length = 200)
    private String hotelName;

    @Column(name = "hotel_address", length = 500)
    private String hotelAddress;

    @Column(name = "room_type", length = 100)
    private String roomType;

    @Column(name = "check_in")
    private LocalDateTime checkIn;

    @Column(name = "check_out")
    private LocalDateTime checkOut;

    @Column(name = "star_rating")
    private Integer starRating;

    // Для трансферов
    @Column(name = "pickup_location", length = 200)
    private String pickupLocation;

    @Column(name = "dropoff_location", length = 200)
    private String dropoffLocation;

    @Column(name = "vehicle_type", length = 100)
    private String vehicleType;

    @Version
    private Long version;
}

