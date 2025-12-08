package com.projectlume.servlet;

import com.projectlume.controller.DashboardController;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Dashboard servlet for the main application page
 * Delegates to DashboardController which manages communication between presentation, domain, and data layers
 */
@WebServlet(name = "DashboardServlet", urlPatterns = {"/dashboard"})
public class DashboardServlet extends HttpServlet {

    private final DashboardController dashboardController;

    public DashboardServlet() {
        this.dashboardController = new DashboardController();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Check if user is logged in
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        // Delegate to controller (manages communication between presentation, domain, and data layers)
        // Controller uses multi-threading for parallel processing
        dashboardController.getDashboardData(request, response);

        // Forward to view
        request.getRequestDispatcher("/WEB-INF/views/dashboard.jsp")
                .forward(request, response);
    }
}

