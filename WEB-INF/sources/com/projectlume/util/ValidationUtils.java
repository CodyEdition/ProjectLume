package com.projectlume.util;

import com.projectlume.exception.ValidationException;

/**
 * Utility class for input validation
 * Provides common validation methods for user input
 */
public class ValidationUtils {
    
    /**
     * Validates username input
     * @param username Username to validate
     * @throws ValidationException if validation fails
     */
    public static void validateUsername(String username) throws ValidationException {
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("username", "Username cannot be empty");
        }
        if (username.length() < 3) {
            throw new ValidationException("username", "Username must be at least 3 characters long");
        }
        if (username.length() > 50) {
            throw new ValidationException("username", "Username must be less than 50 characters");
        }
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            throw new ValidationException("username", "Username can only contain letters, numbers, and underscores");
        }
    }
    
    /**
     * Validates email input
     * @param email Email to validate
     * @throws ValidationException if validation fails
     */
    public static void validateEmail(String email) throws ValidationException {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("email", "Email cannot be empty");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$")) {
            throw new ValidationException("email", "Invalid email format");
        }
        if (email.length() > 100) {
            throw new ValidationException("email", "Email must be less than 100 characters");
        }
    }
    
    /**
     * Validates password input
     * @param password Password to validate
     * @throws ValidationException if validation fails
     */
    public static void validatePassword(String password) throws ValidationException {
        if (password == null || password.trim().isEmpty()) {
            throw new ValidationException("password", "Password cannot be empty");
        }
        if (password.length() < 6) {
            throw new ValidationException("password", "Password must be at least 6 characters long");
        }
        if (password.length() > 255) {
            throw new ValidationException("password", "Password must be less than 255 characters");
        }
    }
    
    /**
     * Validates name input (first name or last name)
     * @param name Name to validate
     * @param fieldName Field name for error messages
     * @throws ValidationException if validation fails
     */
    public static void validateName(String name, String fieldName) throws ValidationException {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException(fieldName, fieldName + " cannot be empty");
        }
        if (name.length() > 50) {
            throw new ValidationException(fieldName, fieldName + " must be less than 50 characters");
        }
    }
    
    /**
     * Validates deck name input
     * @param name Deck name to validate
     * @throws ValidationException if validation fails
     */
    public static void validateDeckName(String name) throws ValidationException {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("deckName", "Deck name cannot be empty");
        }
        if (name.length() > 100) {
            throw new ValidationException("deckName", "Deck name must be less than 100 characters");
        }
    }
    
    /**
     * Validates card text input
     * @param text Card text to validate
     * @param fieldName Field name for error messages
     * @throws ValidationException if validation fails
     */
    public static void validateCardText(String text, String fieldName) throws ValidationException {
        if (text == null || text.trim().isEmpty()) {
            throw new ValidationException(fieldName, fieldName + " cannot be empty");
        }
        if (text.length() > 1000) {
            throw new ValidationException(fieldName, fieldName + " must be less than 1000 characters");
        }
    }
    
    /**
     * Validates ID input
     * @param id ID to validate
     * @param fieldName Field name for error messages
     * @throws ValidationException if validation fails
     */
    public static void validateId(Long id, String fieldName) throws ValidationException {
        if (id == null) {
            throw new ValidationException(fieldName, fieldName + " cannot be null");
        }
        if (id <= 0) {
            throw new ValidationException(fieldName, fieldName + " must be a positive number");
        }
    }
    
    /**
     * Sanitizes string input by trimming whitespace
     * @param input Input string to sanitize
     * @return Sanitized string
     */
    public static String sanitizeString(String input) {
        return input != null ? input.trim() : null;
    }
}
