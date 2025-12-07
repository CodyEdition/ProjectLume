package com.projectlume.filter;

import com.projectlume.model.User;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Authentication filter implementing Filter pattern
 * Handles authentication checks for protected resources
 * Extracts authentication logic from servlets
 */
@WebFilter(filterName = "AuthenticationFilter", urlPatterns = {"/*"})
public class AuthenticationFilter implements Filter {
    private static final Logger logger = Logger.getLogger(AuthenticationFilter.class.getName());
    
    // Public paths that don't require authentication
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
        "/auth",
        "/auth/",
        "/auth/login",
        "/auth/register",
        "/index.html",
        "/"
    );
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("AuthenticationFilter initialized");
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
        
        // Check if path is public
        if (isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }
        
        // Check authentication
        HttpSession session = httpRequest.getSession(false);
        User user = null;
        
        if (session != null) {
            user = (User) session.getAttribute("user");
        }
        
        if (user == null) {
            // User not authenticated, redirect to login
            logger.info("Unauthenticated access attempt to: " + path);
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/auth");
            return;
        }
        
        // User is authenticated, continue with request
        chain.doFilter(request, response);
    }
    
    @Override
    public void destroy() {
        logger.info("AuthenticationFilter destroyed");
    }
    
    /**
     * Check if the path is public (doesn't require authentication)
     */
    private boolean isPublicPath(String path) {
        // Exact match
        if (PUBLIC_PATHS.contains(path)) {
            return true;
        }
        
        // Check if path starts with any public path
        for (String publicPath : PUBLIC_PATHS) {
            if (path.startsWith(publicPath + "/") || path.equals(publicPath)) {
                return true;
            }
        }
        
        // Static resources
        if (path.startsWith("/assets/") || 
            path.startsWith("/WEB-INF/views/login.jsp") ||
            path.startsWith("/WEB-INF/views/register.jsp")) {
            return true;
        }
        
        return false;
    }
}

