package com.projectlume.servlet;

import com.projectlume.command.Command;
import com.projectlume.command.CommandExecutor;
import com.projectlume.command.impl.LoginCommand;
import com.projectlume.command.impl.RegisterCommand;
import com.projectlume.di.ServiceLocator;
import com.projectlume.service.AuthService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.logging.Logger;

@WebServlet(name = "AuthServlet", urlPatterns = {"/auth/*"})
public class AuthServlet extends BaseServlet {
    private static final Logger logger = Logger.getLogger(AuthServlet.class.getName());
    private final AuthService authService;
    private final CommandExecutor commandExecutor;
    
    public AuthServlet() {
        this.authService = ServiceLocator.getAuthService();
        this.commandExecutor = new CommandExecutor();
    }
    
    @Override
    protected boolean requiresAuthentication() {
        return false;
    }
    
    @Override
    protected void processGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String pathInfo = getPathInfo(request);
        
        if (pathInfo.isEmpty()) {
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
        } else if (pathInfo.equals("/register")) {
            request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
        } else if (pathInfo.equals("/logout")) {
            handleLogout(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    @Override
    protected void processPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String pathInfo = getPathInfo(request);
        
        if (pathInfo.isEmpty()) {
            Command loginCommand = new LoginCommand(authService);
            commandExecutor.execute(loginCommand, request, response);
        } else if (pathInfo.equals("/register")) {
            Command registerCommand = new RegisterCommand(authService);
            commandExecutor.execute(registerCommand, request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    private void handleLogout(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            String username = (String) session.getAttribute("username");
            session.invalidate();
            logger.info("User logged out: " + username);
        }
        
        response.sendRedirect(request.getContextPath() + "/auth");
    }
}
