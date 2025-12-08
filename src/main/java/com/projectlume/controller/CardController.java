package com.projectlume.controller;

import com.projectlume.model.Card;
import com.projectlume.model.Deck;
import com.projectlume.service.CardService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Controller for managing card-related operations.
 * Coordinates communication between presentation layer (servlets),
 * domain layer (CardService), and data layer (CardDAO).
 */
public class CardController extends BaseController {
    private final CardService cardService;
    
    public CardController() {
        this.cardService = new CardService();
    }
    
    /**
     * List cards for a specific deck
     * Coordinates: Servlet -> Controller -> Service -> DAO
     */
    public void listCards(HttpServletRequest request, HttpServletResponse response, Long deckId) 
            throws IOException {
        try {
            Long userId = getCurrentUserId(request);
            if (userId == null) {
                requireAuthentication(request, response);
                return;
            }
            
            // Coordinate: Controller calls Service (domain layer)
            Deck deck = cardService.getDeckForUser(userId, deckId);
            List<Card> cards = cardService.getCardsForDeck(userId, deckId);
            
            // Set attributes for presentation layer
            setUserAttribute(request);
            request.setAttribute("deck", deck);
            request.setAttribute("cards", cards);
            
        } catch (SQLException e) {
            logger.severe("Error listing cards: " + e.getMessage());
            setUserAttribute(request);
            request.setAttribute("error", "Failed to load cards");
        } catch (SecurityException e) {
            try {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            } catch (IOException ioException) {
                logger.severe("Failed to send error response: " + ioException.getMessage());
            }
        }
    }
    
    /**
     * Show new card form
     * Coordinates: Servlet -> Controller -> Service -> DAO
     */
    public void showNewCardForm(HttpServletRequest request, HttpServletResponse response, Long deckId) 
            throws IOException {
        try {
            Long userId = getCurrentUserId(request);
            if (userId == null) {
                requireAuthentication(request, response);
                return;
            }
            
            // Coordinate: Controller calls Service (domain layer)
            Deck deck = cardService.getDeckForUser(userId, deckId);
            
            // Set attributes for presentation layer
            setUserAttribute(request);
            request.setAttribute("deck", deck);
            
        } catch (NumberFormatException e) {
            try {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } catch (IOException ioException) {
                logger.severe("Failed to send error response: " + ioException.getMessage());
            }
        } catch (SQLException e) {
            logger.severe("Error loading deck for new card: " + e.getMessage());
            setUserAttribute(request);
            request.setAttribute("error", "Failed to load deck");
        } catch (SecurityException e) {
            try {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            } catch (IOException ioException) {
                logger.severe("Failed to send error response: " + ioException.getMessage());
            }
        }
    }
    
    /**
     * Create a new card
     * Coordinates: Servlet -> Controller -> Service -> DAO
     */
    public String createCard(HttpServletRequest request, HttpServletResponse response, 
                             Long deckId, String frontText, String backText, Card.DifficultyLevel difficultyLevel) 
            throws IOException {
        try {
            Long userId = getCurrentUserId(request);
            if (userId == null) {
                requireAuthentication(request, response);
                return null;
            }
            
            // Coordinate: Controller calls Service (domain layer)
            cardService.createCard(userId, deckId, frontText, backText, difficultyLevel);
            
            return request.getContextPath() + "/card?deckId=" + deckId;
            
        } catch (IllegalArgumentException e) {
            setUserAttribute(request);
            request.setAttribute("error", e.getMessage());
            return null; // Indicates error, servlet should forward to form
        } catch (SQLException e) {
            logger.severe("Error creating card: " + e.getMessage());
            setUserAttribute(request);
            request.setAttribute("error", "Failed to create card");
            return null;
        } catch (SecurityException e) {
            try {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            } catch (IOException ioException) {
                logger.severe("Failed to send error response: " + ioException.getMessage());
            }
            return null;
        }
    }
    
    /**
     * Show edit card form
     * Coordinates: Servlet -> Controller -> Service -> DAO
     */
    public void showEditForm(HttpServletRequest request, HttpServletResponse response, Long cardId) 
            throws IOException {
        try {
            Long userId = getCurrentUserId(request);
            if (userId == null) {
                requireAuthentication(request, response);
                return;
            }
            
            // Coordinate: Controller calls Service (domain layer)
            Card card = cardService.getCardForUser(userId, cardId);
            Deck deck = cardService.getDeckForUser(userId, card.getDeckId());
            
            // Set attributes for presentation layer
            setUserAttribute(request);
            request.setAttribute("card", card);
            request.setAttribute("deck", deck);
            
        } catch (NumberFormatException e) {
            try {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } catch (IOException ioException) {
                logger.severe("Failed to send error response: " + ioException.getMessage());
            }
        } catch (SQLException e) {
            logger.severe("Error loading card for edit: " + e.getMessage());
            setUserAttribute(request);
            request.setAttribute("error", "Failed to load card");
        } catch (SecurityException e) {
            try {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            } catch (IOException ioException) {
                logger.severe("Failed to send error response: " + ioException.getMessage());
            }
        }
    }
    
    /**
     * Update card
     * Coordinates: Servlet -> Controller -> Service -> DAO
     */
    public String updateCard(HttpServletRequest request, HttpServletResponse response, 
                             Long cardId, String frontText, String backText, Card.DifficultyLevel difficultyLevel) 
            throws IOException {
        try {
            Long userId = getCurrentUserId(request);
            if (userId == null) {
                requireAuthentication(request, response);
                return null;
            }
            
            // Coordinate: Controller calls Service (domain layer)
            Card card = cardService.updateCard(userId, cardId, frontText, backText, difficultyLevel);
            
            return request.getContextPath() + "/card?deckId=" + card.getDeckId();
            
        } catch (IllegalArgumentException e) {
            setUserAttribute(request);
            request.setAttribute("error", e.getMessage());
            return null; // Indicates error, servlet should forward to form
        } catch (SQLException e) {
            logger.severe("Error updating card: " + e.getMessage());
            setUserAttribute(request);
            request.setAttribute("error", "Failed to update card");
            return null;
        } catch (SecurityException e) {
            try {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            } catch (IOException ioException) {
                logger.severe("Failed to send error response: " + ioException.getMessage());
            }
            return null;
        }
    }
    
    /**
     * Delete card
     * Coordinates: Servlet -> Controller -> Service -> DAO
     */
    public String deleteCard(HttpServletRequest request, HttpServletResponse response, Long cardId) 
            throws IOException {
        try {
            Long userId = getCurrentUserId(request);
            if (userId == null) {
                requireAuthentication(request, response);
                return null;
            }
            
            // Get card first to get deckId for redirect
            Card card = cardService.getCardForUser(userId, cardId);
            Long deckId = card.getDeckId();
            
            // Coordinate: Controller calls Service (domain layer)
            cardService.deleteCard(userId, cardId);
            
            return request.getContextPath() + "/card?deckId=" + deckId;
            
        } catch (NumberFormatException e) {
            try {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } catch (IOException ioException) {
                logger.severe("Failed to send error response: " + ioException.getMessage());
            }
            return null;
        } catch (SQLException e) {
            logger.severe("Error deleting card: " + e.getMessage());
            return request.getContextPath() + "/deck";
        } catch (SecurityException e) {
            try {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            } catch (IOException ioException) {
                logger.severe("Failed to send error response: " + ioException.getMessage());
            }
            return null;
        }
    }
    
    /**
     * Get card for user (helper method)
     * Coordinates: Controller -> Service -> DAO
     */
    public Card getCardForUser(Long userId, Long cardId) throws SQLException {
        return cardService.getCardForUser(userId, cardId);
    }
    
    /**
     * Get deck for user (helper method)
     * Coordinates: Controller -> Service -> DAO
     */
    public Deck getDeckForUser(Long userId, Long deckId) throws SQLException {
        return cardService.getDeckForUser(userId, deckId);
    }
}

