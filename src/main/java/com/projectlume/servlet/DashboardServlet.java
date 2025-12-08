package com.projectlume.servlet;

import com.projectlume.controller.DashboardController;
import com.projectlume.dto.DashDeckStatsDTO;
import com.projectlume.dto.DeckStatsDTO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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

        // Format deck statistics for dashboard view
        @SuppressWarnings("unchecked")
        List<DeckStatsDTO> deckStatsList = (List<DeckStatsDTO>) request.getAttribute("deckStats");
        if (deckStatsList != null) {
            List<DashDeckStatsDTO> formattedStatsList = new ArrayList<>();
            for (DeckStatsDTO deckStats : deckStatsList) {
                formattedStatsList.add(formatStats(deckStats));
            }
            request.setAttribute("formattedStatsList", formattedStatsList);
        }

        // Forward to view
        request.getRequestDispatcher("/WEB-INF/views/dashboard.jsp")
                .forward(request, response);
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

