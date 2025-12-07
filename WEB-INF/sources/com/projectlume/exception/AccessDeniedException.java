package com.projectlume.exception;

/**
 * Exception thrown when access is denied
 */
public class AccessDeniedException extends ProjectLumeException {
    public AccessDeniedException(String resource) {
        super("ACCESS_DENIED", "Access denied to " + resource);
    }
    
    public AccessDeniedException(String resource, String reason) {
        super("ACCESS_DENIED", "Access denied to " + resource + ": " + reason);
    }
}
