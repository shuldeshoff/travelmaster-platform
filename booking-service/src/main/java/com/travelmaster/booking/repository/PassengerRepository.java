package com.travelmaster.booking.repository;

import com.travelmaster.booking.entity.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PassengerRepository extends JpaRepository<Passenger, Long> {

    @Query("SELECT p FROM Passenger p WHERE p.booking.id = :bookingId")
    List<Passenger> findByBookingId(@Param("bookingId") Long bookingId);
}

