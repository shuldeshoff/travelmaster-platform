package com.travelmaster.notification.repository;

import com.travelmaster.notification.entity.Notification;
import com.travelmaster.notification.entity.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserId(Long userId, Pageable pageable);

    List<Notification> findByStatus(NotificationStatus status);

    @Query("SELECT n FROM Notification n WHERE n.status = 'FAILED' " +
           "AND n.retryCount < 3 " +
           "AND n.nextRetryAt <= :now")
    List<Notification> findRetryableNotifications(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.status = :status")
    long countByStatus(@Param("status") NotificationStatus status);

    @Query("SELECT n FROM Notification n WHERE n.userId = :userId " +
           "AND n.createdAt >= :since " +
           "ORDER BY n.createdAt DESC")
    List<Notification> findRecentByUserId(@Param("userId") Long userId, 
                                          @Param("since") LocalDateTime since);

    List<Notification> findByBookingId(Long bookingId);
}

