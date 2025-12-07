package com.projectlume.strategy.impl;

import com.projectlume.exception.ValidationException;
import com.projectlume.strategy.ValidationStrategy;

/**
 * Validation strategy for email addresses
 */
public class EmailValidationStrategy implements ValidationStrategy {
    
    @Override
    public void validate(String value, String fieldName) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName, "Email cannot be empty");
        }
        if (!value.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$")) {
            throw new ValidationException(fieldName, "Invalid email format");
        }
        if (value.length() > 100) {
            throw new ValidationException(fieldName, "Email must be less than 100 characters");
        }
    }
}

