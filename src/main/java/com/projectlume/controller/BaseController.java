package com.projectlume.controller;

import com.projectlume.model.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Base controller class that manages communication and processing between
 * the presentation (servlets), domain (services), and data (DAOs) layers.
 * 
 * This abstract class provides common coordination logic for all controllers,
 * handling request processing flow, exception handling, and user session validation.
 */
public abstract class BaseController {
    protected static final Logger logger = Logger.getLogger(BaseController.class.getName());
    
    /**
     * Get the current user from the session
     * @param request HTTP request
     * @return User object or null if not logged in
     */
    protected User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (User) session.getAttribute("user");
    }
    
    /**
     * Get the current user ID from the session
     * @param request HTTP request
     * @return User ID or null if not logged in
     */
    protected Long getCurrentUserId(HttpServletRequest request) {
        User user = getCurrentUser(request);
        return user != null ? user.getId() : null;
    }
    
    /**
     * Check if user is logged in
     * @param request HTTP request
     * @return true if user is logged in, false otherwise
     */
    public boolean isUserLoggedIn(HttpServletRequest request) {
        return getCurrentUser(request) != null;
    }
    
    /**
     * Set user attribute in request for JSP pages
     * @param request HTTP request
     */
    public void setUserAttribute(HttpServletRequest request) {
        User user = getCurrentUser(request);
        if (user != null) {
            request.setAttribute("user", user);
        }
    }
    
    /**
     * Handle exceptions that occur during request processing
     * @param request HTTP request
     * @param response HTTP response
     * @param e Exception that occurred
     * @param operation Description of the operation that failed
     * @throws IOException if response handling fails
     */
    protected void handleException(HttpServletRequest request, HttpServletResponse response, 
                                  Exception e, String operation) throws IOException {
        logger.severe("Error in " + operation + ": " + e.getMessage());
        e.printStackTrace();
        
        setUserAttribute(request);
        
        String errorMessage = e.getMessage();
        if (errorMessage == null || errorMessage.isEmpty()) {
            errorMessage = "An unexpected error occurred. Please try again.";
        }
        request.setAttribute("error", errorMessage);
        
        if (e instanceof SecurityException) {
            try {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            } catch (IOException ioException) {
                logger.severe("Failed to send error response: " + ioException.getMessage());
            }
        } else if (e instanceof IllegalArgumentException) {
            try {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, errorMessage);
            } catch (IOException ioException) {
                logger.severe("Failed to send error response: " + ioException.getMessage());
            }
        } else if (e instanceof SQLException) {
            logger.severe("Database error in " + operation + ": " + e.getMessage());
            request.setAttribute("error", "A database error occurred. Please try again.");
        }
    }
    
    /**
     * Validate that a user is logged in, redirecting to login if not
     * @param request HTTP request
     * @param response HTTP response
     * @return true if user is logged in, false if redirected
     * @throws IOException if redirect fails
     */
    protected boolean requireAuthentication(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        if (!isUserLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return false;
        }
        return true;
    }
}

