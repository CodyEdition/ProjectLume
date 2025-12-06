package com.projectlume.servlet;

import com.projectlume.dto.DashDeckStatsDTO;
import com.projectlume.dto.DeckStatsDTO;
import com.projectlume.model.User;
import com.projectlume.service.DeckService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Dashboard servlet for the main application page
 */
@WebServlet(name = "DashboardServlet", urlPatterns = {"/dashboard"})
public class DashboardServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(DashboardServlet.class.getName());
    private final DeckService deckService;
    
    public DashboardServlet() {
        this.deckService = new DeckService();
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
        
        try {
            User user = (User) session.getAttribute("user");
            Long userId = user.getId();
            
            // Get the user's deck statistics
            List<DeckStatsDTO> deckStatsList = deckService.getDeckStatisticsForUser(userId);
            // Format and add in dashboard-specific statistics (date code, formatted lastStudyDate)
            List<DashDeckStatsDTO> formattedStatsList = new ArrayList<>();
            for (int i = 0; i < deckStatsList.size(); i++) {
                formattedStatsList.add(formatStats(deckStatsList.get(i)));
            }

            request.setAttribute("user", user);
            request.setAttribute("formattedStatsList", formattedStatsList);
            request.setAttribute("currentPage", "dashboard");
            request.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(request, response);
            
        } catch (SQLException e) {
            logger.severe("Error loading dashboard: " + e.getMessage());
            request.setAttribute("error", "Failed to load dashboard");
            request.setAttribute("currentPage", "dashboard");
            request.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(request, response);
        }
    }

    /**
     * Creates a {@link DashDeckStatsDTO} object so that the date code and formatted lastStudyDate
     *  can be passed along without using a scriptlet (as that would violate the MVC pattern.)
     * @param deckStats The {@link DeckStatsDTO} object to derive the formatted stats from.
     * @return An initialized {@link DashDeckStatsDTO} object.
     */
    private DashDeckStatsDTO formatStats(DeckStatsDTO deckStats) {
        String lastStudyDate = null;
        if (deckStats.getLastStudyDate() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d y");
            lastStudyDate = deckStats.getLastStudyDate().format(formatter);
        }
        return new DashDeckStatsDTO(deckStats, deckStats.getDateCode(), lastStudyDate);
    }
}
