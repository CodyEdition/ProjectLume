package com.projectlume.command;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

public class CommandExecutor {
    private static final Logger logger = Logger.getLogger(CommandExecutor.class.getName());
    
    public void execute(Command command, HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            CommandResult result = command.execute(request, response);
            processResult(result, request, response);
        } catch (Exception e) {
            logger.severe("Error executing command: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("error", "An error occurred: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }
    
    private void processResult(CommandResult result, HttpServletRequest request, 
                              HttpServletResponse response) throws ServletException, IOException {
        if (result.isSuccess()) {
            if (result.getRedirectUrl() != null) {
                response.sendRedirect(result.getRedirectUrl());
            } else if (result.getForwardPath() != null) {
                request.getRequestDispatcher(result.getForwardPath()).forward(request, response);
            }
        } else {
            if (result.getErrorMessage() != null) {
                request.setAttribute("error", result.getErrorMessage());
            }
            if (result.getForwardPath() != null) {
                request.getRequestDispatcher(result.getForwardPath()).forward(request, response);
            } else {
                request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
            }
        }
    }
}

