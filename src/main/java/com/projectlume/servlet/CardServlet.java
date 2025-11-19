package com.projectlume.servlet;

import com.projectlume.dao.CardDAO;
import com.projectlume.dao.DeckDAO;
import com.projectlume.factory.DAOFactory;
import com.projectlume.model.Card;
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
 * Servlet for card management (CRUD operations)
 */
@WebServlet(name = "CardServlet", urlPatterns = {"/card/*"})
public class CardServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(CardServlet.class.getName());
    private final CardDAO cardDAO;
    private final DeckDAO deckDAO;
    
    public CardServlet() {
        this.cardDAO = DAOFactory.createCardDAO();
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
            // List cards for a specific deck
            String deckIdParam = request.getParameter("deckId");
            if (deckIdParam != null) {
                listCards(request, response, Long.parseLong(deckIdParam));
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
        } else if (pathInfo.equals("/new")) {
            // Show new card form
            showNewCardForm(request, response);
        } else if (pathInfo.startsWith("/edit/")) {
            // Show edit card form
            showEditForm(request, response);
        } else if (pathInfo.startsWith("/delete/")) {
            // Delete card
            deleteCard(request, response);
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
            // Create new card
            createCard(request, response);
        } else if (pathInfo.startsWith("/edit/")) {
            // Update card
            updateCard(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    /**
     * List cards for a specific deck
     */
    private void listCards(HttpServletRequest request, HttpServletResponse response, Long deckId) 
            throws ServletException, IOException {
        try {
            // Verify deck ownership
            Deck deck = deckDAO.findById(deckId);
            if (deck == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            Long userId = getCurrentUserId(request);
            if (!deck.getUserId().equals(userId)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            
            List<Card> cards = cardDAO.findByDeckId(deckId);
            
            // Set user attribute for header
            setUserAttribute(request);
            
            request.setAttribute("deck", deck);
            request.setAttribute("cards", cards);
            request.getRequestDispatcher("/WEB-INF/views/card-list.jsp").forward(request, response);
            
        } catch (SQLException e) {
            logger.severe("Error listing cards: " + e.getMessage());
            setUserAttribute(request);
            request.setAttribute("error", "Failed to load cards");
            request.getRequestDispatcher("/WEB-INF/views/card-list.jsp").forward(request, response);
        }
    }
    
    /**
     * Show new card form
     */
    private void showNewCardForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String deckIdParam = request.getParameter("deckId");
        if (deckIdParam == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        try {
            Long deckId = Long.parseLong(deckIdParam);
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
            request.getRequestDispatcher("/WEB-INF/views/card-form.jsp").forward(request, response);
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException e) {
            logger.severe("Error loading deck for new card: " + e.getMessage());
            setUserAttribute(request);
            request.setAttribute("error", "Failed to load deck");
            request.getRequestDispatcher("/WEB-INF/views/card-form.jsp").forward(request, response);
        }
    }
    
    /**
     * Create a new card
     */
    private void createCard(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String deckIdParam = request.getParameter("deckId");
            String frontText = request.getParameter("frontText");
            String backText = request.getParameter("backText");
            String difficultyLevelStr = request.getParameter("difficultyLevel");
            
            if (deckIdParam == null || frontText == null || backText == null) {
                request.setAttribute("error", "All fields are required");
                request.getRequestDispatcher("/WEB-INF/views/card-form.jsp").forward(request, response);
                return;
            }
            
            Long deckId = Long.parseLong(deckIdParam);
            
            // Verify deck ownership
            Deck deck = deckDAO.findById(deckId);
            if (deck == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            Long userId = getCurrentUserId(request);
            if (!deck.getUserId().equals(userId)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            
            Card.DifficultyLevel difficultyLevel = Card.DifficultyLevel.MEDIUM;
            if (difficultyLevelStr != null) {
                try {
                    difficultyLevel = Card.DifficultyLevel.valueOf(difficultyLevelStr);
                } catch (IllegalArgumentException e) {
                    // Use default MEDIUM level
                }
            }
            
            Card card = new Card(deckId, frontText, backText, difficultyLevel);
            cardDAO.create(card);
            
            logger.info("Card created successfully: " + frontText);
            response.sendRedirect(request.getContextPath() + "/card?deckId=" + deckId);
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException e) {
            logger.severe("Error creating card: " + e.getMessage());
            setUserAttribute(request);
            request.setAttribute("error", "Failed to create card");
            request.getRequestDispatcher("/WEB-INF/views/card-form.jsp").forward(request, response);
        }
    }
    
    /**
     * Show edit card form
     */
    private void showEditForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            String cardIdStr = pathInfo.substring("/edit/".length());
            Long cardId = Long.parseLong(cardIdStr);
            
            Card card = cardDAO.findById(cardId);
            if (card == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            // Verify deck ownership
            Deck deck = deckDAO.findById(card.getDeckId());
            if (deck == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            Long userId = getCurrentUserId(request);
            if (!deck.getUserId().equals(userId)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            
            setUserAttribute(request);
            request.setAttribute("card", card);
            request.setAttribute("deck", deck);
            request.getRequestDispatcher("/WEB-INF/views/card-form.jsp").forward(request, response);
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException e) {
            logger.severe("Error loading card for edit: " + e.getMessage());
            setUserAttribute(request);
            request.setAttribute("error", "Failed to load card");
            request.getRequestDispatcher("/WEB-INF/views/card-form.jsp").forward(request, response);
        }
    }
    
    /**
     * Update card
     */
    private void updateCard(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            String cardIdStr = pathInfo.substring("/edit/".length());
            Long cardId = Long.parseLong(cardIdStr);
            
            Card card = cardDAO.findById(cardId);
            if (card == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            // Verify deck ownership
            Deck deck = deckDAO.findById(card.getDeckId());
            if (deck == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            Long userId = getCurrentUserId(request);
            if (!deck.getUserId().equals(userId)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            
            String frontText = request.getParameter("frontText");
            String backText = request.getParameter("backText");
            String difficultyLevelStr = request.getParameter("difficultyLevel");
            
            if (frontText == null || backText == null) {
                setUserAttribute(request);
                request.setAttribute("error", "All fields are required");
                request.setAttribute("card", card);
                request.setAttribute("deck", deck);
                request.getRequestDispatcher("/WEB-INF/views/card-form.jsp").forward(request, response);
                return;
            }
            
            card.setFrontText(frontText);
            card.setBackText(backText);
            
            if (difficultyLevelStr != null) {
                try {
                    card.setDifficultyLevel(Card.DifficultyLevel.valueOf(difficultyLevelStr));
                } catch (IllegalArgumentException e) {
                    // Keep current difficulty level
                }
            }
            
            cardDAO.update(card);
            
            logger.info("Card updated successfully: " + frontText);
            response.sendRedirect(request.getContextPath() + "/card?deckId=" + card.getDeckId());
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException e) {
            logger.severe("Error updating card: " + e.getMessage());
            request.setAttribute("error", "Failed to update card");
            request.getRequestDispatcher("/WEB-INF/views/card-form.jsp").forward(request, response);
        }
    }
    
    /**
     * Delete card
     */
    private void deleteCard(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            String cardIdStr = pathInfo.substring("/delete/".length());
            Long cardId = Long.parseLong(cardIdStr);
            
            Card card = cardDAO.findById(cardId);
            if (card == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            // Verify deck ownership
            Deck deck = deckDAO.findById(card.getDeckId());
            if (deck == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            Long userId = getCurrentUserId(request);
            if (!deck.getUserId().equals(userId)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            
            cardDAO.delete(cardId);
            
            logger.info("Card deleted successfully: " + card.getFrontText());
            response.sendRedirect(request.getContextPath() + "/card?deckId=" + card.getDeckId());
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException e) {
            logger.severe("Error deleting card: " + e.getMessage());
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
