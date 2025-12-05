package com.projectlume.servlet;

import com.projectlume.dto.StudySessionDTO;
import com.projectlume.model.User;
import com.projectlume.service.StudyService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Servlet for displaying study history
 * Acts as a controller in the MVC pattern - delegates business logic to StudyService
 */
@WebServlet(name = "StudyHistoryServlet", urlPatterns = {"/study/history"})
public class StudyHistoryServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(StudyHistoryServlet.class.getName());
    private final StudyService studyService;

    public StudyHistoryServlet() {
        this.studyService = new StudyService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Ensure user's logged in; if not, redirect and request authentication.
        if (!isUserLoggedIn(request)) {
            logger.info("User not logged in; redirecting to /auth");
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        try {
            // Get user ID from session
            Long userId = getCurrentUserId(request);
            if (userId == null) {
                response.sendRedirect(request.getContextPath() + "/auth");
                return;
            }

            // Get study history via service (converts to DTOs with formatted data)
            List<StudySessionDTO> sessions = studyService.getStudyHistoryForUser(userId);

            request.setAttribute("sessions", sessions);
            request.setAttribute("currentPage", "studyHistory");

            // Display study history
            request.getRequestDispatcher("/WEB-INF/views/study-history.jsp").forward(request, response);

        } catch (SQLException e) {
            logger.severe("Error fetching study history: " + e.getMessage());
            request.setAttribute("error", "Failed to fetch study history");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }

    /**
     * Check if user is logged in
     */
    private boolean isUserLoggedIn(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute("user") != null;
    }

    /**
     * Get current user ID from session
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        User user = (User) session.getAttribute("user");
        return user != null ? user.getId() : null;
    }
}