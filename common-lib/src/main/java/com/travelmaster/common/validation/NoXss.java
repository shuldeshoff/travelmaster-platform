package com.travelmaster.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validation annotation для проверки отсутствия XSS атак в строках.
 * 
 * Usage:
 * <pre>
 * public class MyDto {
 *     @NoXss
 *     private String userInput;
 * }
 * </pre>
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NoXssValidator.class)
@Documented
public @interface NoXss {
    
    String message() default "Input contains potentially malicious content";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}

