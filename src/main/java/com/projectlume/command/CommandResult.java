package com.projectlume.command;

/**
 * Result of command execution
 * Indicates success/failure and next action to take
 */
public class CommandResult {
    private final boolean success;
    private final String redirectUrl;
    private final String forwardPath;
    private final String errorMessage;
    
    private CommandResult(boolean success, String redirectUrl, String forwardPath, String errorMessage) {
        this.success = success;
        this.redirectUrl = redirectUrl;
        this.forwardPath = forwardPath;
        this.errorMessage = errorMessage;
    }
    
    /**
     * Create a successful result that redirects
     */
    public static CommandResult redirect(String url) {
        return new CommandResult(true, url, null, null);
    }
    
    /**
     * Create a successful result that forwards to a JSP
     */
    public static CommandResult forward(String path) {
        return new CommandResult(true, null, path, null);
    }
    
    /**
     * Create a failure result with error message
     */
    public static CommandResult error(String errorMessage) {
        return new CommandResult(false, null, null, errorMessage);
    }
    
    /**
     * Create a failure result that forwards to error page
     */
    public static CommandResult errorForward(String path, String errorMessage) {
        return new CommandResult(false, null, path, errorMessage);
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public String getRedirectUrl() {
        return redirectUrl;
    }
    
    public String getForwardPath() {
        return forwardPath;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
}

