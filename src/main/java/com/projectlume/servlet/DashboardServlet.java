package com.projectlume.servlet;

import com.projectlume.dao.DeckDAO;
import com.projectlume.factory.DAOFactory;
import com.projectlume.model.Deck;
import com.projectlume.model.User;

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
 * Dashboard servlet for the main application page
 */
@WebServlet(name = "DashboardServlet", urlPatterns = {"/dashboard"})
public class DashboardServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(DashboardServlet.class.getName());
    private final DeckDAO deckDAO;
    
    public DashboardServlet() {
        this.deckDAO = DAOFactory.createDeckDAO();
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
            
            // Get user's decks
            List<Deck> decks = deckDAO.findByUserId(userId);
            
            request.setAttribute("user", user);
            request.setAttribute("decks", decks);
            request.setAttribute("currentPage", "dashboard");
            request.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(request, response);
            
        } catch (SQLException e) {
            logger.severe("Error loading dashboard: " + e.getMessage());
            request.setAttribute("error", "Failed to load dashboard");
            request.setAttribute("currentPage", "dashboard");
            request.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(request, response);
        }
    }
}
