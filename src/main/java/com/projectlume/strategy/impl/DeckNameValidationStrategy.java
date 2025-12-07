package com.projectlume.strategy.impl;

import com.projectlume.exception.ValidationException;
import com.projectlume.strategy.ValidationStrategy;

/**
 * Validation strategy for deck names
 */
public class DeckNameValidationStrategy implements ValidationStrategy {
    
    @Override
    public void validate(String value, String fieldName) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName, "Deck name cannot be empty");
        }
        if (value.length() > 100) {
            throw new ValidationException(fieldName, "Deck name must be less than 100 characters");
        }
    }
}

