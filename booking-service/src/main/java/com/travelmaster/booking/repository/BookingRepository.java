package com.travelmaster.booking.repository;

import com.travelmaster.booking.entity.Booking;
import com.travelmaster.booking.entity.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingReference(String bookingReference);

    Page<Booking> findByUserId(Long userId, Pageable pageable);

    Page<Booking> findByUserIdAndStatus(Long userId, BookingStatus status, Pageable pageable);

    List<Booking> findByTripId(Long tripId);

    @Query("SELECT b FROM Booking b WHERE b.tripId = :tripId AND b.status IN :statuses")
    List<Booking> findByTripIdAndStatusIn(@Param("tripId") Long tripId, @Param("statuses") List<BookingStatus> statuses);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.userId = :userId")
    long countByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = :status")
    long countByStatus(@Param("status") BookingStatus status);

    @Query("SELECT SUM(b.paidAmount) FROM Booking b WHERE b.status = 'PAID' AND b.paidAt BETWEEN :startDate AND :endDate")
    java.math.BigDecimal getTotalRevenueBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    boolean existsByBookingReference(String bookingReference);
}

