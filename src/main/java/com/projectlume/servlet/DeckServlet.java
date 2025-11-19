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
 * Servlet for deck management (CRUD operations)
 */
@WebServlet(name = "DeckServlet", urlPatterns = {"/deck/*"})
public class DeckServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(DeckServlet.class.getName());
    private final DeckDAO deckDAO;
    
    public DeckServlet() {
        this.deckDAO = DAOFactory.createDeckDAO();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Check if user is logged in
        if (!isUserLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            // List all decks for the user
            listDecks(request, response);
        } else if (pathInfo.equals("/new")) {
            // Show new deck form
            setUserAttribute(request);
            request.getRequestDispatcher("/WEB-INF/views/deck-form.jsp").forward(request, response);
        } else if (pathInfo.startsWith("/edit/")) {
            // Show edit deck form
            showEditForm(request, response);
        } else if (pathInfo.startsWith("/delete/")) {
            // Delete deck
            deleteDeck(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Check if user is logged in
        if (!isUserLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            // Create new deck
            createDeck(request, response);
        } else if (pathInfo.startsWith("/edit/")) {
            // Update deck
            updateDeck(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    /**
     * List all decks for the current user
     */
    private void listDecks(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            Long userId = getCurrentUserId(request);
            List<Deck> decks = deckDAO.findByUserId(userId);
            
            request.setAttribute("decks", decks);
            request.setAttribute("currentPage", "dashboard");
            // Reuse dashboard view to list decks; dedicated deck-list.jsp not present
            request.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(request, response);
            
        } catch (SQLException e) {
            logger.severe("Error listing decks: " + e.getMessage());
            request.setAttribute("error", "Failed to load decks");
            request.setAttribute("currentPage", "dashboard");
            request.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(request, response);
        }
    }
    
    /**
     * Create a new deck
     */
    private void createDeck(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String name = request.getParameter("name");
            String description = request.getParameter("description");
            Long userId = getCurrentUserId(request);
            
            if (name == null || name.trim().isEmpty()) {
                setUserAttribute(request);
                request.setAttribute("error", "Deck name is required");
                request.getRequestDispatcher("/WEB-INF/views/deck-form.jsp").forward(request, response);
                return;
            }
            
            Deck deck = new Deck(userId, name, description);
            deckDAO.create(deck);
            
            logger.info("Deck created successfully: " + name);
            response.sendRedirect(request.getContextPath() + "/deck");
            
        } catch (SQLException e) {
            logger.severe("Error creating deck: " + e.getMessage());
            setUserAttribute(request);
            request.setAttribute("error", "Failed to create deck");
            request.getRequestDispatcher("/WEB-INF/views/deck-form.jsp").forward(request, response);
        }
    }
    
    /**
     * Show edit deck form
     */
    private void showEditForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            String deckIdStr = pathInfo.substring("/edit/".length());
            Long deckId = Long.parseLong(deckIdStr);
            
            Deck deck = deckDAO.findById(deckId);
            if (deck == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            // Check if user owns this deck
            Long userId = getCurrentUserId(request);
            if (!deck.getUserId().equals(userId)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            
            setUserAttribute(request);
            request.setAttribute("deck", deck);
            request.getRequestDispatcher("/WEB-INF/views/deck-form.jsp").forward(request, response);
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException e) {
            logger.severe("Error loading deck for edit: " + e.getMessage());
            request.setAttribute("error", "Failed to load deck");
            response.sendRedirect(request.getContextPath() + "/deck");
        }
    }
    
    /**
     * Update deck
     */
    private void updateDeck(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            String deckIdStr = pathInfo.substring("/edit/".length());
            Long deckId = Long.parseLong(deckIdStr);
            
            Deck deck = deckDAO.findById(deckId);
            if (deck == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            // Check if user owns this deck
            Long userId = getCurrentUserId(request);
            if (!deck.getUserId().equals(userId)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            
            String name = request.getParameter("name");
            String description = request.getParameter("description");
            
            if (name == null || name.trim().isEmpty()) {
                setUserAttribute(request);
                request.setAttribute("error", "Deck name is required");
                request.setAttribute("deck", deck);
                request.getRequestDispatcher("/WEB-INF/views/deck-form.jsp").forward(request, response);
                return;
            }
            
            deck.setName(name);
            deck.setDescription(description);
            deckDAO.update(deck);
            
            logger.info("Deck updated successfully: " + name);
            response.sendRedirect(request.getContextPath() + "/deck");
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException e) {
            logger.severe("Error updating deck: " + e.getMessage());
            setUserAttribute(request);
            request.setAttribute("error", "Failed to update deck");
            request.getRequestDispatcher("/WEB-INF/views/deck-form.jsp").forward(request, response);
        }
    }
    
    /**
     * Delete deck
     */
    private void deleteDeck(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            String deckIdStr = pathInfo.substring("/delete/".length());
            Long deckId = Long.parseLong(deckIdStr);
            
            Deck deck = deckDAO.findById(deckId);
            if (deck == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            // Check if user owns this deck
            Long userId = getCurrentUserId(request);
            if (!deck.getUserId().equals(userId)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            
            deckDAO.delete(deckId);
            
            logger.info("Deck deleted successfully: " + deck.getName());
            response.sendRedirect(request.getContextPath() + "/deck");
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException e) {
            logger.severe("Error deleting deck: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/deck");
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
    
    /**
     * Set user attribute in request for JSP pages
     */
    private void setUserAttribute(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            User user = (User) session.getAttribute("user");
            if (user != null) {
                request.setAttribute("user", user);
            }
        }
    }
}
