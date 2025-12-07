package com.projectlume.servlet;

import com.projectlume.model.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.logging.Logger;

public abstract class BaseServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(BaseServlet.class.getName());
    
    @Override
    protected final void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            if (!requiresAuthentication() || isUserLoggedIn(request)) {
                if (!requiresAuthorization() || checkAuthorization(request, response)) {
                    processGet(request, response);
                }
            } else {
                redirectToLogin(request, response);
            }
        } catch (Exception e) {
            handleException(request, response, e, "GET");
        }
    }
    
    @Override
    protected final void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            if (!requiresAuthentication() || isUserLoggedIn(request)) {
                if (!requiresAuthorization() || checkAuthorization(request, response)) {
                    processPost(request, response);
                }
            } else {
                redirectToLogin(request, response);
            }
        } catch (Exception e) {
            handleException(request, response, e, "POST");
        }
    }
    
    protected abstract void processGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException;
    
    protected abstract void processPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException;
    
    protected boolean requiresAuthentication() {
        return true;
    }
    
    protected boolean requiresAuthorization() {
        return false;
    }
    
    protected boolean checkAuthorization(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        return true;
    }
    
    protected boolean isUserLoggedIn(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute("user") != null;
    }
    
    protected User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (User) session.getAttribute("user");
    }
    
    protected Long getCurrentUserId(HttpServletRequest request) {
        User user = getCurrentUser(request);
        return user != null ? user.getId() : null;
    }
    
    protected void setUserAttribute(HttpServletRequest request) {
        User user = getCurrentUser(request);
        if (user != null) {
            request.setAttribute("user", user);
        }
    }
    
    protected void redirectToLogin(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        response.sendRedirect(request.getContextPath() + "/auth");
    }
    
    protected void handleException(HttpServletRequest request, HttpServletResponse response, 
                                  Exception e, String method) throws ServletException, IOException {
        logger.severe("Error in " + getClass().getSimpleName() + "." + method + ": " + e.getMessage());
        e.printStackTrace();
        
        setUserAttribute(request);
        
        String errorMessage = e.getMessage();
        if (errorMessage == null || errorMessage.isEmpty()) {
            errorMessage = "An unexpected error occurred. Please try again.";
        }
        request.setAttribute("error", errorMessage);
        
        if (e instanceof SecurityException) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        } else if (e instanceof IllegalArgumentException) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, errorMessage);
        } else {
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }
    
    protected Long parseIdFromPath(String pathInfo, String prefix) {
        if (pathInfo == null || !pathInfo.startsWith(prefix)) {
            return null;
        }
        try {
            String idStr = pathInfo.substring(prefix.length());
            return Long.parseLong(idStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    protected String getPathInfo(HttpServletRequest request) {
        String pathInfo = request.getPathInfo();
        return (pathInfo == null || pathInfo.equals("/")) ? "" : pathInfo;
    }
}

