package com.projectlume.servlet;

import com.projectlume.controller.CardController;
import com.projectlume.model.Card;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet for card management (CRUD operations)
 * Delegates to CardController which manages communication between presentation, domain, and data layers
 */
@WebServlet(name = "CardServlet", urlPatterns = {"/card/*"})
public class CardServlet extends HttpServlet {
    private final CardController cardController;
    
    public CardServlet() {
        this.cardController = new CardController();
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
     * Delegates to CardController which coordinates between layers
     */
    private void listCards(HttpServletRequest request, HttpServletResponse response, Long deckId) 
            throws ServletException, IOException {
        // Delegate to controller (manages communication between presentation, domain, and data layers)
        cardController.listCards(request, response, deckId);
        request.getRequestDispatcher("/WEB-INF/views/card-list.jsp").forward(request, response);
    }
    
    /**
     * Show new card form
     * Delegates to CardController which coordinates between layers
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
            // Delegate to controller (manages communication between presentation, domain, and data layers)
            cardController.showNewCardForm(request, response, deckId);
            request.getRequestDispatcher("/WEB-INF/views/card-form.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
    
    /**
     * Create a new card
     * Delegates to CardController which coordinates between layers
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
            
            // Parse difficulty level
            Card.DifficultyLevel difficultyLevel = null;
            if (difficultyLevelStr != null) {
                try {
                    difficultyLevel = Card.DifficultyLevel.valueOf(difficultyLevelStr);
                } catch (IllegalArgumentException e) {
                    // Will use default in service
                }
            }
            
            // Delegate to controller (manages communication between presentation, domain, and data layers)
            String redirectUrl = cardController.createCard(request, response, deckId, frontText, backText, difficultyLevel);
            
            if (redirectUrl != null) {
                response.sendRedirect(redirectUrl);
            } else {
                request.getRequestDispatcher("/WEB-INF/views/card-form.jsp").forward(request, response);
            }
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
    
    /**
     * Show edit card form
     * Delegates to CardController which coordinates between layers
     */
    private void showEditForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            String cardIdStr = pathInfo.substring("/edit/".length());
            Long cardId = Long.parseLong(cardIdStr);
            
            // Delegate to controller (manages communication between presentation, domain, and data layers)
            cardController.showEditForm(request, response, cardId);
            request.getRequestDispatcher("/WEB-INF/views/card-form.jsp").forward(request, response);
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
    
    /**
     * Update card
     * Delegates to CardController which coordinates between layers
     */
    private void updateCard(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            String cardIdStr = pathInfo.substring("/edit/".length());
            Long cardId = Long.parseLong(cardIdStr);
            
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
            
            // Delegate to controller (manages communication between presentation, domain, and data layers)
            String redirectUrl = cardController.updateCard(request, response, cardId, frontText, backText, difficultyLevel);
            
            if (redirectUrl != null) {
                response.sendRedirect(redirectUrl);
            } else {
                // Try to reload card and deck for form
                try {
                    cardController.showEditForm(request, response, cardId);
                } catch (Exception ex) {
                    // Ignore - will show error only
                }
                request.getRequestDispatcher("/WEB-INF/views/card-form.jsp").forward(request, response);
            }
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
    
    /**
     * Delete card
     * Delegates to CardController which coordinates between layers
     */
    private void deleteCard(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            String cardIdStr = pathInfo.substring("/delete/".length());
            Long cardId = Long.parseLong(cardIdStr);
            
            // Delegate to controller (manages communication between presentation, domain, and data layers)
            String redirectUrl = cardController.deleteCard(request, response, cardId);
            
            if (redirectUrl != null) {
                response.sendRedirect(redirectUrl);
            } else {
                response.sendRedirect(request.getContextPath() + "/deck");
            }
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
    
    /**
     * Check if user is logged in
     */
    private boolean isUserLoggedIn(HttpServletRequest request) {
        return cardController.isUserLoggedIn(request);
    }
}
