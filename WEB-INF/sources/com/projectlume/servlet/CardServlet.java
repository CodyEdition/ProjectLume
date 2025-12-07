package com.projectlume.servlet;

import com.projectlume.model.Card;
import com.projectlume.model.Deck;
import com.projectlume.model.User;
import com.projectlume.service.CardService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Servlet for card management (CRUD operations)
 * Acts as a controller in the MVC pattern - delegates business logic to CardService
 */
@WebServlet(name = "CardServlet", urlPatterns = {"/card/*"})
public class CardServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(CardServlet.class.getName());
    private final CardService cardService;
    
    public CardServlet() {
        this.cardService = new CardService();
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
            Long userId = getCurrentUserId(request);
            
            // Get deck and cards via service (validates ownership)
            Deck deck = cardService.getDeckForUser(userId, deckId);
            java.util.List<Card> cards = cardService.getCardsForDeck(userId, deckId);
            
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
        } catch (SecurityException e) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
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
            Long userId = getCurrentUserId(request);
            
            // Get deck via service (validates ownership)
            Deck deck = cardService.getDeckForUser(userId, deckId);
            
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
        } catch (SecurityException e) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
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
            
            if (deckIdParam == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            Long deckId = Long.parseLong(deckIdParam);
            Long userId = getCurrentUserId(request);
            
            // Parse difficulty level
            Card.DifficultyLevel difficultyLevel = null;
            if (difficultyLevelStr != null) {
                try {
                    difficultyLevel = Card.DifficultyLevel.valueOf(difficultyLevelStr);
                } catch (IllegalArgumentException e) {
                    // Will use default in service
                }
            }
            
            // Create card via service (validates ownership and input)
            cardService.createCard(userId, deckId, frontText, backText, difficultyLevel);
            
            response.sendRedirect(request.getContextPath() + "/card?deckId=" + deckId);
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (IllegalArgumentException e) {
            setUserAttribute(request);
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/card-form.jsp").forward(request, response);
        } catch (SQLException e) {
            logger.severe("Error creating card: " + e.getMessage());
            setUserAttribute(request);
            request.setAttribute("error", "Failed to create card");
            request.getRequestDispatcher("/WEB-INF/views/card-form.jsp").forward(request, response);
        } catch (SecurityException e) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
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
            Long userId = getCurrentUserId(request);
            
            // Get card and deck via service (validates ownership)
            Card card = cardService.getCardForUser(userId, cardId);
            Deck deck = cardService.getDeckForUser(userId, card.getDeckId());
            
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
        } catch (SecurityException e) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
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
            Long userId = getCurrentUserId(request);
            
            String frontText = request.getParameter("frontText");
            String backText = request.getParameter("backText");
            String difficultyLevelStr = request.getParameter("difficultyLevel");
            
            // Parse difficulty level
            Card.DifficultyLevel difficultyLevel = null;
            if (difficultyLevelStr != null) {
                try {
                    difficultyLevel = Card.DifficultyLevel.valueOf(difficultyLevelStr);
                } catch (IllegalArgumentException e) {
                    // Will keep current in service
                }
            }
            
            // Update card via service (validates ownership and input)
            Card card = cardService.updateCard(userId, cardId, frontText, backText, difficultyLevel);
            
            response.sendRedirect(request.getContextPath() + "/card?deckId=" + card.getDeckId());
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (IllegalArgumentException e) {
            setUserAttribute(request);
            request.setAttribute("error", e.getMessage());
            // Try to reload card and deck for form
            try {
                String pathInfo = request.getPathInfo();
                Long cardId = Long.parseLong(pathInfo.substring("/edit/".length()));
                Long userId = getCurrentUserId(request);
                Card card = cardService.getCardForUser(userId, cardId);
                Deck deck = cardService.getDeckForUser(userId, card.getDeckId());
                request.setAttribute("card", card);
                request.setAttribute("deck", deck);
            } catch (Exception ex) {
                // Ignore - will show error only
            }
            request.getRequestDispatcher("/WEB-INF/views/card-form.jsp").forward(request, response);
        } catch (SQLException e) {
            logger.severe("Error updating card: " + e.getMessage());
            setUserAttribute(request);
            request.setAttribute("error", "Failed to update card");
            request.getRequestDispatcher("/WEB-INF/views/card-form.jsp").forward(request, response);
        } catch (SecurityException e) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
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
            Long userId = getCurrentUserId(request);
            
            // Get card first to get deckId for redirect
            Card card = cardService.getCardForUser(userId, cardId);
            Long deckId = card.getDeckId();
            
            // Delete card via service (validates ownership)
            cardService.deleteCard(userId, cardId);
            
            response.sendRedirect(request.getContextPath() + "/card?deckId=" + deckId);
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException e) {
            logger.severe("Error deleting card: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/deck");
        } catch (SecurityException e) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
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
