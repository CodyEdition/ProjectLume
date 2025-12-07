package com.projectlume.strategy.impl;

import com.projectlume.exception.ValidationException;
import com.projectlume.strategy.ValidationStrategy;

/**
 * Validation strategy for names (first name, last name)
 */
public class NameValidationStrategy implements ValidationStrategy {
    
    @Override
    public void validate(String value, String fieldName) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName, fieldName + " cannot be empty");
        }
        if (value.length() > 50) {
            throw new ValidationException(fieldName, fieldName + " must be less than 50 characters");
        }
    }
}

