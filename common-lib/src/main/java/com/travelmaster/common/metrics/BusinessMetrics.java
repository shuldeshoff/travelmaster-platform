package com.travelmaster.common.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Централизованное управление бизнес-метриками.
 */
@Component
public class BusinessMetrics {

    private final MeterRegistry meterRegistry;

    public BusinessMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Регистрация события создания бронирования.
     */
    public void recordBookingCreated(String status) {
        Counter.builder("booking.created")
                .tag("status", status)
                .description("Number of bookings created")
                .register(meterRegistry)
                .increment();
    }

    /**
     * Регистрация успешного платежа.
     */
    public void recordPaymentSuccess(double amount, String currency) {
        Counter.builder("payment.success")
                .tag("currency", currency)
                .description("Number of successful payments")
                .register(meterRegistry)
                .increment();

        meterRegistry.counter("payment.amount", "currency", currency)
                .increment(amount);
    }

    /**
     * Регистрация отправки уведомления.
     */
    public void recordNotificationSent(String type, String channel, boolean success) {
        Counter.builder("notification.sent")
                .tag("type", type)
                .tag("channel", channel)
                .tag("success", String.valueOf(success))
                .description("Number of notifications sent")
                .register(meterRegistry)
                .increment();
    }

    /**
     * Замер времени выполнения операции.
     */
    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }

    /**
     * Завершение замера времени.
     */
    public void recordTimer(Timer.Sample sample, String operationName) {
        sample.stop(Timer.builder("operation.duration")
                .tag("operation", operationName)
                .description("Operation execution time")
                .register(meterRegistry));
    }

    /**
     * Регистрация ошибки.
     */
    public void recordError(String service, String errorType) {
        Counter.builder("error.count")
                .tag("service", service)
                .tag("type", errorType)
                .description("Number of errors")
                .register(meterRegistry)
                .increment();
    }
}

