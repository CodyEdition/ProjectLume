package com.projectlume.exception;

/**
 * Exception thrown when validation fails
 */
public class ValidationException extends ProjectLumeException {
    public ValidationException(String message) {
        super("VALIDATION_ERROR", message);
    }
    
    public ValidationException(String field, String message) {
        super("VALIDATION_ERROR", "Validation failed for " + field + ": " + message);
    }
}
