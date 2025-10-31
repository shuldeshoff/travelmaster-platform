package com.travelmaster.payment.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publisher для отправки событий платежей в Kafka.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String PAYMENT_EVENTS_TOPIC = "payment-events";

    public void publishPaymentCreated(PaymentCreatedEvent event) {
        log.info("Publishing PaymentCreatedEvent for payment: {}", event.getPaymentId());
        kafkaTemplate.send(PAYMENT_EVENTS_TOPIC, event.getPaymentReference(), event);
    }

    public void publishPaymentProcessed(PaymentProcessedEvent event) {
        log.info("Publishing PaymentProcessedEvent for payment: {}", event.getPaymentId());
        kafkaTemplate.send(PAYMENT_EVENTS_TOPIC, event.getPaymentReference(), event);
    }

    public void publishPaymentFailed(PaymentFailedEvent event) {
        log.info("Publishing PaymentFailedEvent for payment: {}", event.getPaymentId());
        kafkaTemplate.send(PAYMENT_EVENTS_TOPIC, event.getPaymentReference(), event);
    }

    public void publishPaymentRefunded(PaymentRefundedEvent event) {
        log.info("Publishing PaymentRefundedEvent for payment: {}", event.getPaymentId());
        kafkaTemplate.send(PAYMENT_EVENTS_TOPIC, event.getPaymentReference(), event);
    }
}

