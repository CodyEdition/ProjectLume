package com.projectlume.controller;

import com.projectlume.dto.DeckStatsDTO;
import com.projectlume.model.Deck;
import com.projectlume.service.DeckService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Controller for managing deck-related operations.
 */
public class DeckController extends BaseController {
    private final DeckService deckService;
    
    public DeckController() {
        this.deckService = new DeckService();
    }
    
    /**
     * List all decks for the current user
     */
    public void listDecks(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        try {
            Long userId = getCurrentUserId(request);
            if (userId == null) {
                requireAuthentication(request, response);
                return;
            }
            
            // Controller calls Service (domain layer)
            List<DeckStatsDTO> deckStatsList = deckService.getDeckStatisticsForUser(userId);
            
            // Set attributes for presentation layer
            request.setAttribute("deckStatsList", deckStatsList);
            request.setAttribute("currentPage", "dashboard");
            
        } catch (SQLException e) {
            logger.severe("Error listing decks: " + e.getMessage());
            request.setAttribute("error", "Failed to load decks");
            request.setAttribute("currentPage", "dashboard");
        }
    }
    
    /**
     * Create a new deck
     */
    public String createDeck(HttpServletRequest request, HttpServletResponse response, 
                             String name, String description) throws IOException {
        try {
            Long userId = getCurrentUserId(request);
            if (userId == null) {
                requireAuthentication(request, response);
                return null;
            }
            
            // Controller calls Service (domain layer)
            deckService.createDeck(userId, name, description);
            
            return request.getContextPath() + "/deck";
            
        } catch (IllegalArgumentException e) {
            setUserAttribute(request);
            request.setAttribute("error", e.getMessage());
            return null;
        } catch (SQLException e) {
            logger.severe("Error creating deck: " + e.getMessage());
            setUserAttribute(request);
            request.setAttribute("error", "Failed to create deck");
            return null;
        }
    }
    
    /**
     * Show edit deck form
     */
    public void showEditForm(HttpServletRequest request, HttpServletResponse response, Long deckId) 
            throws IOException {
        try {
            Long userId = getCurrentUserId(request);
            if (userId == null) {
                requireAuthentication(request, response);
                return;
            }
            
            // Controller calls Service (domain layer)
            Deck deck = deckService.getDeckForUser(userId, deckId);
            
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
            logger.severe("Error loading deck for edit: " + e.getMessage());
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
     * Update deck
     */
    public String updateDeck(HttpServletRequest request, HttpServletResponse response, 
                            Long deckId, String name, String description) throws IOException {
        try {
            Long userId = getCurrentUserId(request);
            if (userId == null) {
                requireAuthentication(request, response);
                return null;
            }
            
            // Controller calls Service (domain layer)
            deckService.updateDeck(userId, deckId, name, description);
            
            return request.getContextPath() + "/deck";
            
        } catch (NumberFormatException e) {
            try {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } catch (IOException ioException) {
                logger.severe("Failed to send error response: " + ioException.getMessage());
            }
            return null;
        } catch (IllegalArgumentException e) {
            setUserAttribute(request);
            request.setAttribute("error", e.getMessage());
            return null;
        } catch (SQLException e) {
            logger.severe("Error updating deck: " + e.getMessage());
            setUserAttribute(request);
            request.setAttribute("error", "Failed to update deck");
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
     * Delete deck
     */
    public String deleteDeck(HttpServletRequest request, HttpServletResponse response, Long deckId) 
            throws IOException {
        try {
            Long userId = getCurrentUserId(request);
            if (userId == null) {
                requireAuthentication(request, response);
                return null;
            }
            
            // Controller calls Service (domain layer)
            deckService.deleteDeck(userId, deckId);
            
            return request.getContextPath() + "/deck";
            
        } catch (NumberFormatException e) {
            try {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } catch (IOException ioException) {
                logger.severe("Failed to send error response: " + ioException.getMessage());
            }
            return null;
        } catch (SQLException e) {
            logger.severe("Error deleting deck: " + e.getMessage());
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
     * Get deck for user (helper method)
     */
    public Deck getDeckForUser(Long userId, Long deckId) throws SQLException {
        return deckService.getDeckForUser(userId, deckId);
    }
    
    /**
     * Get deck statistics for user (helper method)
     */
    public List<DeckStatsDTO> getDeckStatisticsForUser(Long userId) throws SQLException {
        return deckService.getDeckStatisticsForUser(userId);
    }
}

