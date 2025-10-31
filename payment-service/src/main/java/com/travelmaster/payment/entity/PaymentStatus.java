package com.travelmaster.payment.entity;

/**
 * Статусы платежа.
 */
public enum PaymentStatus {
    PENDING,      // Создан, ожидает обработки
    PROCESSING,   // В процессе обработки
    SUCCESS,      // Успешно обработан
    FAILED,       // Ошибка при обработке
    REFUNDED,     // Возвращен
    CANCELLED     // Отменен
}

