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

import java.math.BigDecimal;
import java.util.Map;

/**
 * Kafka consumer для событий платежей.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "payment-events", groupId = "notification-service")
    public void consumePaymentEvent(String message) {
        try {
            log.info("Received payment event: {}", message);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            
            String eventType = determineEventType(event);
            processPaymentEvent(eventType, event);
            
        } catch (Exception e) {
            log.error("Error processing payment event: {}", e.getMessage(), e);
        }
    }

    private String determineEventType(Map<String, Object> event) {
        if (event.containsKey("processedAt")) {
            return "PaymentProcessedEvent";
        } else if (event.containsKey("failedAt")) {
            return "PaymentFailedEvent";
        } else if (event.containsKey("refundedAt")) {
            return "PaymentRefundedEvent";
        }
        return "Unknown";
    }

    private void processPaymentEvent(String eventType, Map<String, Object> event) {
        Long userId = getLong(event, "userId");
        String paymentReference = getString(event, "paymentReference");
        Long paymentId = getLong(event, "paymentId");
        Long bookingId = getLong(event, "bookingId");

        Notification notification = Notification.builder()
                .userId(userId)
                .recipientEmail(getUserEmail(userId))
                .channel(NotificationChannel.EMAIL)
                .paymentId(paymentId)
                .bookingId(bookingId)
                .build();

        switch (eventType) {
            case "PaymentProcessedEvent" -> {
                notification.setType(NotificationType.PAYMENT_SUCCESS);
                notification.setSubject("Платеж успешно обработан");
                BigDecimal amount = getBigDecimal(event, "amount");
                String currency = getString(event, "currency");
                notification.setContent(String.format(
                        "Здравствуйте! Ваш платеж на сумму %.2f %s успешно обработан. " +
                        "Номер платежа: %s",
                        amount, currency, paymentReference
                ));
            }
            case "PaymentFailedEvent" -> {
                notification.setType(NotificationType.PAYMENT_FAILED);
                notification.setSubject("Ошибка при обработке платежа");
                String reason = getString(event, "failureReason");
                notification.setContent(String.format(
                        "Здравствуйте! К сожалению, ваш платеж не был обработан. " +
                        "Причина: %s. Пожалуйста, попробуйте снова или свяжитесь с поддержкой.",
                        reason != null ? reason : "неизвестна"
                ));
            }
            case "PaymentRefundedEvent" -> {
                notification.setType(NotificationType.PAYMENT_REFUNDED);
                notification.setSubject("Возврат средств выполнен");
                BigDecimal refundAmount = getBigDecimal(event, "refundAmount");
                notification.setContent(String.format(
                        "Здравствуйте! Возврат средств в размере %.2f выполнен. " +
                        "Средства поступят на вашу карту в течение 3-5 рабочих дней.",
                        refundAmount
                ));
            }
            default -> {
                log.warn("Unknown payment event type: {}", eventType);
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

    private BigDecimal getBigDecimal(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        return BigDecimal.ZERO;
    }

    private String getUserEmail(Long userId) {
        // TODO: Получить email из User Service
        return "user" + userId + "@example.com";
    }
}

