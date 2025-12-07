package com.projectlume.strategy.impl;

import com.projectlume.exception.ValidationException;
import com.projectlume.strategy.ValidationStrategy;

/**
 * Validation strategy for card text (front and back)
 */
public class CardTextValidationStrategy implements ValidationStrategy {
    
    @Override
    public void validate(String value, String fieldName) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName, fieldName + " cannot be empty");
        }
        if (value.length() > 1000) {
            throw new ValidationException(fieldName, fieldName + " must be less than 1000 characters");
        }
    }
}

