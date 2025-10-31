package com.travelmaster.booking.statemachine;

/**
 * События State Machine для бронирования.
 */
public enum BookingEvent {
    CONFIRM,   // Подтверждение бронирования
    PAY,       // Оплата
    COMPLETE,  // Завершение (поездка состоялась)
    CANCEL     // Отмена
}

