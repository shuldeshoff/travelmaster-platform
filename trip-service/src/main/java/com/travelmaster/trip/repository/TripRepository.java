package com.travelmaster.trip.repository;

import com.travelmaster.trip.entity.Trip;
import com.travelmaster.trip.entity.TripStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {

    @Query("SELECT t FROM Trip t WHERE t.status = :status")
    Page<Trip> findByStatus(@Param("status") TripStatus status, Pageable pageable);

    @Query("SELECT t FROM Trip t WHERE " +
           "LOWER(t.origin) LIKE LOWER(CONCAT('%', :origin, '%')) AND " +
           "LOWER(t.destination) LIKE LOWER(CONCAT('%', :destination, '%')) AND " +
           "t.departureDate >= :startDate AND " +
           "t.departureDate <= :endDate AND " +
           "t.availableSeats >= :passengers AND " +
           "t.status = 'AVAILABLE'")
    Page<Trip> searchTrips(
            @Param("origin") String origin,
            @Param("destination") String destination,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("passengers") Integer passengers,
            Pageable pageable
    );

    @Query("SELECT t FROM Trip t WHERE " +
           "t.price BETWEEN :minPrice AND :maxPrice AND " +
           "t.status = 'AVAILABLE'")
    Page<Trip> findByPriceRange(
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );

    @Query("SELECT t FROM Trip t WHERE t.provider = :provider")
    List<Trip> findByProvider(@Param("provider") String provider);

    @Query("SELECT t FROM Trip t WHERE t.providerId = :providerId AND t.provider = :provider")
    Trip findByProviderIdAndProvider(@Param("providerId") String providerId, @Param("provider") String provider);

    @Query("SELECT COUNT(t) FROM Trip t WHERE t.status = 'AVAILABLE'")
    long countAvailableTrips();
}

