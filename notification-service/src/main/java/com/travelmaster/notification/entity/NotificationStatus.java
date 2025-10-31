package com.travelmaster.notification.entity;

/**
 * Статусы уведомлений.
 */
public enum NotificationStatus {
    PENDING,    // Ожидает отправки
    SENDING,    // В процессе отправки
    SENT,       // Успешно отправлено
    FAILED      // Ошибка при отправке
}

