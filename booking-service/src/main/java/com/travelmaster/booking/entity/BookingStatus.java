package com.travelmaster.booking.entity;

/**
 * Статусы бронирования.
 * 
 * Flow:
 * PENDING → CONFIRMED → PAID → COMPLETED
 * 
 * Отмена возможна на любом этапе до COMPLETED:
 * * → CANCELLED
 */
public enum BookingStatus {
    PENDING,      // Создано, ожидает подтверждения
    CONFIRMED,    // Подтверждено, места зарезервированы
    PAID,         // Оплачено
    COMPLETED,    // Завершено (поездка состоялась)
    CANCELLED     // Отменено
}

