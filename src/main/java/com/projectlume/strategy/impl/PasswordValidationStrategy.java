package com.projectlume.strategy.impl;

import com.projectlume.exception.ValidationException;
import com.projectlume.strategy.ValidationStrategy;

/**
 * Validation strategy for passwords
 */
public class PasswordValidationStrategy implements ValidationStrategy {
    
    @Override
    public void validate(String value, String fieldName) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName, "Password cannot be empty");
        }
        if (value.length() < 6) {
            throw new ValidationException(fieldName, "Password must be at least 6 characters long");
        }
        if (value.length() > 255) {
            throw new ValidationException(fieldName, "Password must be less than 255 characters");
        }
    }
}

