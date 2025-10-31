package com.travelmaster.notification.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelmaster.notification.entity.Notification;
import com.travelmaster.notification.entity.NotificationChannel;
import com.travelmaster.notification.entity.NotificationType;
import com.travelmaster.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Kafka consumer для событий бронирований.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookingEventConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "booking-events", groupId = "notification-service")
    public void consumeBookingEvent(String message) {
        try {
            log.info("Received booking event: {}", message);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            
            String eventType = determineEventType(event);
            processBookingEvent(eventType, event);
            
        } catch (Exception e) {
            log.error("Error processing booking event: {}", e.getMessage(), e);
        }
    }

    private String determineEventType(Map<String, Object> event) {
        // Определяем тип события по полям
        if (event.containsKey("confirmedAt")) {
            return "BookingConfirmedEvent";
        } else if (event.containsKey("cancelledAt")) {
            return "BookingCancelledEvent";
        } else if (event.containsKey("paidAt")) {
            return "BookingPaidEvent";
        } else if (event.containsKey("createdAt") && !event.containsKey("confirmedAt")) {
            return "BookingCreatedEvent";
        } else if (event.containsKey("completedAt")) {
            return "BookingCompletedEvent";
        }
        return "Unknown";
    }

    private void processBookingEvent(String eventType, Map<String, Object> event) {
        Long userId = getLong(event, "userId");
        String bookingReference = getString(event, "bookingReference");
        Long bookingId = getLong(event, "bookingId");

        Notification notification = Notification.builder()
                .userId(userId)
                .recipientEmail(getUserEmail(userId))
                .channel(NotificationChannel.EMAIL)
                .bookingId(bookingId)
                .build();

        switch (eventType) {
            case "BookingCreatedEvent" -> {
                notification.setType(NotificationType.BOOKING_CREATED);
                notification.setSubject("Ваше бронирование создано");
                notification.setContent(String.format(
                        "Здравствуйте! Ваше бронирование %s успешно создано. " +
                        "Пожалуйста, завершите оплату в течение 24 часов.",
                        bookingReference
                ));
            }
            case "BookingConfirmedEvent" -> {
                notification.setType(NotificationType.BOOKING_CONFIRMED);
                notification.setSubject("Ваше бронирование подтверждено");
                notification.setContent(String.format(
                        "Здравствуйте! Ваше бронирование %s подтверждено. " +
                        "Места успешно зарезервированы.",
                        bookingReference
                ));
            }
            case "BookingPaidEvent" -> {
                notification.setType(NotificationType.PAYMENT_SUCCESS);
                notification.setSubject("Платеж успешно обработан");
                notification.setContent(String.format(
                        "Здравствуйте! Ваш платеж для бронирования %s успешно обработан. " +
                        "Спасибо за использование TravelMaster!",
                        bookingReference
                ));
            }
            case "BookingCancelledEvent" -> {
                notification.setType(NotificationType.BOOKING_CANCELLED);
                notification.setSubject("Ваше бронирование отменено");
                String reason = getString(event, "cancellationReason");
                notification.setContent(String.format(
                        "Здравствуйте! Ваше бронирование %s было отменено. Причина: %s",
                        bookingReference, reason != null ? reason : "не указана"
                ));
            }
            case "BookingCompletedEvent" -> {
                notification.setType(NotificationType.BOOKING_COMPLETED);
                notification.setSubject("Ваша поездка завершена");
                notification.setContent(String.format(
                        "Здравствуйте! Ваша поездка по бронированию %s завершена. " +
                        "Надеемся, вам понравилось! Оставьте отзыв.",
                        bookingReference
                ));
            }
            default -> {
                log.warn("Unknown booking event type: {}", eventType);
                return;
            }
        }

        notificationService.createAndSend(notification);
    }

    private Long getLong(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return null;
    }

    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    private String getUserEmail(Long userId) {
        // TODO: Получить email из User Service
        // Пока используем заглушку
        return "user" + userId + "@example.com";
    }
}

