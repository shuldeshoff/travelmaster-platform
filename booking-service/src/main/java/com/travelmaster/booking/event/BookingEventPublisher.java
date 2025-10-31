package com.travelmaster.booking.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publisher для отправки событий бронирования в Kafka.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookingEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String BOOKING_EVENTS_TOPIC = "booking-events";

    public void publishBookingCreated(BookingCreatedEvent event) {
        log.info("Publishing BookingCreatedEvent for booking: {}", event.getBookingId());
        kafkaTemplate.send(BOOKING_EVENTS_TOPIC, event.getBookingId().toString(), event);
    }

    public void publishBookingConfirmed(BookingConfirmedEvent event) {
        log.info("Publishing BookingConfirmedEvent for booking: {}", event.getBookingId());
        kafkaTemplate.send(BOOKING_EVENTS_TOPIC, event.getBookingId().toString(), event);
    }

    public void publishBookingPaid(BookingPaidEvent event) {
        log.info("Publishing BookingPaidEvent for booking: {}", event.getBookingId());
        kafkaTemplate.send(BOOKING_EVENTS_TOPIC, event.getBookingId().toString(), event);
    }

    public void publishBookingCancelled(BookingCancelledEvent event) {
        log.info("Publishing BookingCancelledEvent for booking: {}", event.getBookingId());
        kafkaTemplate.send(BOOKING_EVENTS_TOPIC, event.getBookingId().toString(), event);
    }

    public void publishBookingCompleted(BookingCompletedEvent event) {
        log.info("Publishing BookingCompletedEvent for booking: {}", event.getBookingId());
        kafkaTemplate.send(BOOKING_EVENTS_TOPIC, event.getBookingId().toString(), event);
    }
}

