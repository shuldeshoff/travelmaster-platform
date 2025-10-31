package com.travelmaster.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validation annotation для проверки отсутствия SQL Injection атак.
 * 
 * Примечание: JPA/Hibernate с prepared statements уже защищает от SQL injection,
 * но эта валидация - дополнительный уровень защиты для raw queries.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NoSqlInjectionValidator.class)
@Documented
public @interface NoSqlInjection {
    
    String message() default "Input contains potentially malicious SQL content";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}

