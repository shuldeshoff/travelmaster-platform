package com.travelmaster.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Validator для проверки отсутствия XSS атак.
 * 
 * Проверяет наличие потенциально опасных HTML/JavaScript паттернов.
 */
public class NoXssValidator implements ConstraintValidator<NoXss, String> {

    // Паттерны для обнаружения XSS атак
    private static final Pattern[] XSS_PATTERNS = {
        Pattern.compile("<script", Pattern.CASE_INSENSITIVE),
        Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("onerror=", Pattern.CASE_INSENSITIVE),
        Pattern.compile("onload=", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<iframe", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<object", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<embed", Pattern.CASE_INSENSITIVE),
        Pattern.compile("eval\\(", Pattern.CASE_INSENSITIVE),
        Pattern.compile("expression\\(", Pattern.CASE_INSENSITIVE)
    };

    @Override
    public void initialize(NoXss constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true; // null/empty handled by @NotNull/@NotBlank
        }

        // Check for XSS patterns
        for (Pattern pattern : XSS_PATTERNS) {
            if (pattern.matcher(value).find()) {
                return false;
            }
        }

        return true;
    }
}

