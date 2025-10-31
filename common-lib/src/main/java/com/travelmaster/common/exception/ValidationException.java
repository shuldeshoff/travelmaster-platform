package com.travelmaster.common.exception;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Исключение для ошибок валидации.
 */
@Getter
public class ValidationException extends BusinessException {

    private final Map<String, String> validationErrors;

    public ValidationException(String message) {
        super("VALIDATION_ERROR", message);
        this.validationErrors = new HashMap<>();
    }

    public ValidationException(String message, Map<String, String> validationErrors) {
        super("VALIDATION_ERROR", message);
        this.validationErrors = validationErrors;
    }

    public ValidationException addError(String field, String error) {
        this.validationErrors.put(field, error);
        return this;
    }
}

