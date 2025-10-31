package com.travelmaster.notification.entity;

/**
 * Типы уведомлений.
 */
public enum NotificationType {
    BOOKING_CREATED,        // Бронирование создано
    BOOKING_CONFIRMED,      // Бронирование подтверждено
    BOOKING_CANCELLED,      // Бронирование отменено
    BOOKING_COMPLETED,      // Поездка завершена
    
    PAYMENT_SUCCESS,        // Платеж успешен
    PAYMENT_FAILED,         // Платеж неудачен
    PAYMENT_REFUNDED,       // Возврат средств
    
    USER_REGISTERED,        // Новый пользователь
    USER_PASSWORD_RESET,    // Сброс пароля
    
    TRIP_UPDATED,           // Изменения в поездке
    TRIP_REMINDER           // Напоминание о поездке
}

