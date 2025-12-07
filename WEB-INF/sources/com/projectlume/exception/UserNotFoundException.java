package com.projectlume.exception;

/**
 * Exception thrown when a user is not found
 */
public class UserNotFoundException extends ProjectLumeException {
    public UserNotFoundException(String username) {
        super("USER_NOT_FOUND", "User not found: " + username);
    }
    
    public UserNotFoundException(Long userId) {
        super("USER_NOT_FOUND", "User not found with ID: " + userId);
    }
}
