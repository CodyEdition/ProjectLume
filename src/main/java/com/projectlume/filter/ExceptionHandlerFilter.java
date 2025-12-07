package com.projectlume.filter;

import com.projectlume.exception.ProjectLumeException;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Global exception handler filter
 * Centralizes exception handling and converts exceptions to appropriate HTTP responses
 */
@WebFilter(filterName = "ExceptionHandlerFilter", urlPatterns = {"/*"})
public class ExceptionHandlerFilter implements Filter {
    private static final Logger logger = Logger.getLogger(ExceptionHandlerFilter.class.getName());
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("ExceptionHandlerFilter initialized");
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        try {
            chain.doFilter(request, response);
        } catch (Exception e) {
            handleException(e, httpRequest, httpResponse);
        }
    }
    
    @Override
    public void destroy() {
        logger.info("ExceptionHandlerFilter destroyed");
    }
    
    /**
     * Handle exceptions and convert to appropriate HTTP responses
     */
    private void handleException(Exception e, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        logger.severe("Exception caught in filter: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        e.printStackTrace();
        
        // Set error attribute for error page
        String errorMessage = getErrorMessage(e);
        request.setAttribute("error", errorMessage);
        
        // Determine HTTP status code based on exception type
        int statusCode = getStatusCode(e);
        
        // Set user attribute if available
        try {
            var session = request.getSession(false);
            if (session != null && session.getAttribute("user") != null) {
                request.setAttribute("user", session.getAttribute("user"));
            }
        } catch (Exception ex) {
            // Ignore - user may not be logged in
        }
        
        // Send appropriate response
        if (statusCode == HttpServletResponse.SC_FORBIDDEN) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, errorMessage);
        } else if (statusCode == HttpServletResponse.SC_BAD_REQUEST) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, errorMessage);
        } else if (statusCode == HttpServletResponse.SC_NOT_FOUND) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, errorMessage);
        } else {
            // Forward to error page for other exceptions
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }
    
    /**
     * Get user-friendly error message from exception
     */
    private String getErrorMessage(Exception e) {
        if (e instanceof ProjectLumeException) {
            return e.getMessage();
        } else if (e instanceof SQLException) {
            return "A database error occurred. Please try again later.";
        } else if (e instanceof SecurityException) {
            return "Access denied. You do not have permission to perform this action.";
        } else if (e instanceof IllegalArgumentException) {
            return "Invalid input: " + e.getMessage();
        } else {
            return "An unexpected error occurred. Please try again.";
        }
    }
    
    /**
     * Get HTTP status code based on exception type
     */
    private int getStatusCode(Exception e) {
        if (e instanceof SecurityException) {
            return HttpServletResponse.SC_FORBIDDEN;
        } else if (e instanceof IllegalArgumentException) {
            return HttpServletResponse.SC_BAD_REQUEST;
        } else if (e instanceof SQLException) {
            // Check if it's a "not found" type error
            String message = e.getMessage();
            if (message != null && (message.contains("not found") || message.contains("Not found"))) {
                return HttpServletResponse.SC_NOT_FOUND;
            }
            return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        } else {
            return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        }
    }
}

