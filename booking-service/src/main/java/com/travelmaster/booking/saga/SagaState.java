package com.travelmaster.booking.saga;

/**
 * Состояния выполнения Saga.
 */
public enum SagaState {
    STARTED,           // Saga запущена
    SEATS_RESERVED,    // Места зарезервированы
    BOOKING_CONFIRMED, // Бронирование подтверждено
    PAYMENT_INITIATED, // Платеж инициирован
    PAYMENT_COMPLETED, // Платеж завершен
    COMPLETED,         // Saga успешно завершена
    COMPENSATING,      // Выполняются компенсирующие транзакции
    FAILED             // Saga провалена
}

