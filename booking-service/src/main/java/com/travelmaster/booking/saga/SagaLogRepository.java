package com.travelmaster.booking.saga;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SagaLogRepository extends JpaRepository<SagaLog, Long> {

    List<SagaLog> findByBookingIdOrderByCreatedAtAsc(Long bookingId);
    
    List<SagaLog> findByStateOrderByCreatedAtDesc(SagaState state);
}

