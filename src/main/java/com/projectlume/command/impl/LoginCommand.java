package com.projectlume.command.impl;

import com.projectlume.command.Command;
import com.projectlume.command.CommandResult;
import com.projectlume.model.User;
import com.projectlume.service.AuthService;
import com.projectlume.service.AuthService.AuthException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Command for handling user login
 */
public class LoginCommand implements Command {
    private final AuthService authService;
    
    public LoginCommand(AuthService authService) {
        this.authService = authService;
    }
    
    @Override
    public CommandResult execute(HttpServletRequest request, HttpServletResponse response) {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        
        try {
            User user = authService.login(username, password);
            
            // Create session
            HttpSession session = request.getSession(true);
            session.setAttribute("user", user);
            session.setAttribute("userId", user.getId());
            session.setAttribute("username", user.getUsername());
            
            // Redirect to dashboard
            return CommandResult.redirect(request.getContextPath() + "/dashboard");
            
        } catch (AuthException e) {
            request.setAttribute("error", e.getMessage());
            return CommandResult.errorForward("/WEB-INF/views/login.jsp", e.getMessage());
        }
    }
}

