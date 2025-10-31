package com.travelmaster.common.exception;

/**
 * Исключение когда entity не найдена.
 */
public class EntityNotFoundException extends BusinessException {

    public EntityNotFoundException(String entityName, Long id) {
        super("NOT_FOUND", String.format("%s with id %d not found", entityName, id));
    }

    public EntityNotFoundException(String message) {
        super("NOT_FOUND", message);
    }
}

