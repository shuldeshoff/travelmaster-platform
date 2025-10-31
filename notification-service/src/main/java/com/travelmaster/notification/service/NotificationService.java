package com.travelmaster.notification.service;

import com.travelmaster.notification.entity.Notification;
import com.travelmaster.notification.entity.NotificationChannel;
import com.travelmaster.notification.entity.NotificationStatus;
import com.travelmaster.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Основной сервис для управления уведомлениями.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    /**
     * Создание и отправка уведомления.
     */
    @Transactional
    public Notification createAndSend(Notification notification) {
        log.info("Creating notification for user {}: {}", 
                notification.getUserId(), notification.getType());

        notification.setStatus(NotificationStatus.PENDING);
        notification = notificationRepository.save(notification);

        try {
            sendNotification(notification);
            notification.markAsSent();
        } catch (Exception e) {
            log.error("Failed to send notification {}: {}", 
                    notification.getId(), e.getMessage());
            notification.markAsFailed(e.getMessage());
        }

        return notificationRepository.save(notification);
    }

    /**
     * Отправка уведомления в зависимости от канала.
     */
    private void sendNotification(Notification notification) {
        notification.setStatus(NotificationStatus.SENDING);
        notificationRepository.save(notification);

        switch (notification.getChannel()) {
            case EMAIL -> emailService.sendEmail(notification);
            case SMS -> sendSMS(notification);
            case PUSH -> sendPush(notification);
        }
    }

    /**
     * Отправка SMS (mock реализация).
     */
    private void sendSMS(Notification notification) {
        log.info("Sending SMS to {}: {}", 
                notification.getRecipientPhone(), notification.getSubject());
        // TODO: Реализовать интеграцию с SMS провайдером
    }

    /**
     * Отправка Push уведомления (mock реализация).
     */
    private void sendPush(Notification notification) {
        log.info("Sending PUSH notification to user {}: {}", 
                notification.getUserId(), notification.getSubject());
        // TODO: Реализовать интеграцию с FCM/APNs
    }

    /**
     * Повторная отправка failed уведомлений.
     * Выполняется каждые 5 минут.
     */
    @Scheduled(fixedDelay = 300000) // 5 minutes
    @Transactional
    public void retryFailedNotifications() {
        List<Notification> retryable = notificationRepository
                .findRetryableNotifications(LocalDateTime.now());

        if (retryable.isEmpty()) {
            return;
        }

        log.info("Retrying {} failed notifications", retryable.size());

        for (Notification notification : retryable) {
            try {
                sendNotification(notification);
                notification.markAsSent();
                log.info("Successfully retried notification {}", notification.getId());
            } catch (Exception e) {
                log.error("Retry failed for notification {}: {}", 
                        notification.getId(), e.getMessage());
                notification.markAsFailed(e.getMessage());
            }
            
            notificationRepository.save(notification);
        }
    }

    /**
     * Получение уведомлений пользователя.
     */
    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(Long userId) {
        LocalDateTime since = LocalDateTime.now().minusDays(30); // Последние 30 дней
        return notificationRepository.findRecentByUserId(userId, since);
    }

    /**
     * Получение статистики по уведомлениям.
     */
    @Transactional(readOnly = true)
    public NotificationStats getStats() {
        return NotificationStats.builder()
                .pending(notificationRepository.countByStatus(NotificationStatus.PENDING))
                .sent(notificationRepository.countByStatus(NotificationStatus.SENT))
                .failed(notificationRepository.countByStatus(NotificationStatus.FAILED))
                .build();
    }

    @lombok.Data
    @lombok.Builder
    public static class NotificationStats {
        private long pending;
        private long sent;
        private long failed;
    }
}

