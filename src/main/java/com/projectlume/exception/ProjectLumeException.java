package com.projectlume.exception;

/**
 * Base exception class for Project Lume application
 * Provides common error handling functionality
 */
public class ProjectLumeException extends Exception {
    private final String errorCode;
    
    public ProjectLumeException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public ProjectLumeException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
