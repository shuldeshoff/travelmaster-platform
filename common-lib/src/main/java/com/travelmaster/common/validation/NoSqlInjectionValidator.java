package com.travelmaster.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Validator для проверки отсутствия SQL Injection атак.
 */
public class NoSqlInjectionValidator implements ConstraintValidator<NoSqlInjection, String> {

    // Паттерны для обнаружения SQL injection
    private static final Pattern[] SQL_INJECTION_PATTERNS = {
        Pattern.compile("('|(\\-\\-)|(;)|(\\|\\|)|(\\*))", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\w*((%27)|('))\\s*((%6F)|o|(%4F))((%72)|r|(%52))", Pattern.CASE_INSENSITIVE),
        Pattern.compile("((%3D)|(=))[^\\n]*(((%27)|('))|((%3B)|(;)))", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\w*((%27)|('))\\s*((%75)|u|(%55))((%6E)|n|(%4E))((%69)|i|(%49))((%6F)|o|(%4F))((%6E)|n|(%4E))", Pattern.CASE_INSENSITIVE),
        Pattern.compile("((%27)|('))\\s*((%6F)|o|(%4F))((%72)|r|(%52))", Pattern.CASE_INSENSITIVE)
    };

    @Override
    public void initialize(NoSqlInjection constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true;
        }

        // Check for SQL injection patterns
        for (Pattern pattern : SQL_INJECTION_PATTERNS) {
            if (pattern.matcher(value).find()) {
                return false;
            }
        }

        return true;
    }
}

