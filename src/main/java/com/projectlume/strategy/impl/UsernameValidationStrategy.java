package com.projectlume.strategy.impl;

import com.projectlume.exception.ValidationException;
import com.projectlume.strategy.ValidationStrategy;

/**
 * Validation strategy for usernames
 */
public class UsernameValidationStrategy implements ValidationStrategy {
    
    @Override
    public void validate(String value, String fieldName) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName, "Username cannot be empty");
        }
        if (value.length() < 3) {
            throw new ValidationException(fieldName, "Username must be at least 3 characters long");
        }
        if (value.length() > 50) {
            throw new ValidationException(fieldName, "Username must be less than 50 characters");
        }
        if (!value.matches("^[a-zA-Z0-9_]+$")) {
            throw new ValidationException(fieldName, "Username can only contain letters, numbers, and underscores");
        }
    }
}

