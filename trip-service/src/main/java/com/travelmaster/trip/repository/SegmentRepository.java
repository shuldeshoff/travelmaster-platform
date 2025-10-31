package com.travelmaster.trip.repository;

import com.travelmaster.trip.entity.Segment;
import com.travelmaster.trip.entity.SegmentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SegmentRepository extends JpaRepository<Segment, Long> {

    @Query("SELECT s FROM Segment s WHERE s.trip.id = :tripId ORDER BY s.order")
    List<Segment> findByTripIdOrderByOrder(@Param("tripId") Long tripId);

    @Query("SELECT s FROM Segment s WHERE s.trip.id = :tripId AND s.type = :type")
    List<Segment> findByTripIdAndType(@Param("tripId") Long tripId, @Param("type") SegmentType type);
}

