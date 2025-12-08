package com.projectlume.controller;

import com.projectlume.model.Card;
import com.projectlume.model.Deck;
import com.projectlume.model.StudySession;
import com.projectlume.service.StudyService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Controller for managing study session operations.
 */
public class StudyController extends BaseController {
    private final StudyService studyService;
    
    public StudyController() {
        this.studyService = new StudyService();
    }
    
    /**
     * Start a study session for a deck
     */
    public StudySession startStudySession(HttpServletRequest request, HttpServletResponse response, Long deckId) 
            throws IOException {
        try {
            Long userId = getCurrentUserId(request);
            if (userId == null) {
                requireAuthentication(request, response);
                return null;
            }
            
            // Controller calls Service (domain layer)
            Deck deck = studyService.getDeckForUser(userId, deckId);
            List<Card> cards = studyService.getCardsForDeck(userId, deckId);
            
            if (cards.isEmpty()) {
                request.setAttribute("error", "This deck has no cards to study");
                request.setAttribute("deck", deck);
                return null;
            }
            
            // Controller calls Service to start session
            StudySession session = studyService.startStudySession(userId, deckId);
            
            // Store study state in session for presentation layer
            HttpSession httpSession = request.getSession();
            httpSession.setAttribute("studySessionId", session.getId());
            httpSession.setAttribute("studyDeckId", deckId);
            httpSession.setAttribute("studyCards", cards);
            httpSession.setAttribute("studyCardIndex", 0);
            httpSession.setAttribute("studyStartTime", System.currentTimeMillis());
            
            // Set attributes for presentation layer
            setUserAttribute(request);
            request.setAttribute("deck", deck);
            
            return session;
            
        } catch (SQLException e) {
            logger.severe("Error starting study session: " + e.getMessage());
            request.setAttribute("error", "Failed to start study session");
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
     * Show card answer (after flip)
     */
    public void showCardAnswer(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        try {
            HttpSession httpSession = request.getSession(false);
            if (httpSession == null) {
                requireAuthentication(request, response);
                return;
            }
            
            Long sessionId = (Long) httpSession.getAttribute("studySessionId");
            Long deckId = (Long) httpSession.getAttribute("studyDeckId");
            @SuppressWarnings("unchecked")
            List<Card> cards = (List<Card>) httpSession.getAttribute("studyCards");
            Integer cardIndex = (Integer) httpSession.getAttribute("studyCardIndex");
            
            if (sessionId == null || deckId == null || cards == null || cardIndex == null) {
                return;
            }
            
            if (cardIndex >= cards.size()) {
                return;
            }
            
            Card currentCard = cards.get(cardIndex);
            Long userId = getCurrentUserId(request);
            
            // Controller calls Service (domain layer)
            Deck deck = studyService.getDeckForUser(userId, deckId);
            
            // Set attributes for presentation layer
            setUserAttribute(request);
            request.setAttribute("card", currentCard);
            request.setAttribute("deck", deck);
            request.setAttribute("cardIndex", cardIndex);
            request.setAttribute("totalCards", cards.size());
            
        } catch (SQLException e) {
            logger.severe("Error showing card answer: " + e.getMessage());
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
     * Record answer and move to next card
     */
    public void recordAnswer(HttpServletRequest request, HttpServletResponse response, 
                             boolean wasCorrect) throws IOException {
        try {
            HttpSession httpSession = request.getSession(false);
            if (httpSession == null) {
                requireAuthentication(request, response);
                return;
            }
            
            Long sessionId = (Long) httpSession.getAttribute("studySessionId");
            Long userId = getCurrentUserId(request);
            @SuppressWarnings("unchecked")
            List<Card> cards = (List<Card>) httpSession.getAttribute("studyCards");
            Integer cardIndex = (Integer) httpSession.getAttribute("studyCardIndex");
            
            if (sessionId == null || cards == null || cardIndex == null) {
                return;
            }
            
            if (cardIndex >= cards.size()) {
                return;
            }
            
            Card currentCard = cards.get(cardIndex);
            
            // Controller calls Service (domain layer)
            studyService.recordCardAnswer(sessionId, currentCard.getId(), userId, wasCorrect, null);
            
            // Move to next card
            int nextIndex = cardIndex + 1;
            httpSession.setAttribute("studyCardIndex", nextIndex);
            
            if (nextIndex < cards.size()) {
                Card nextCard = cards.get(nextIndex);
                Long deckId = (Long) httpSession.getAttribute("studyDeckId");
                Deck deck = studyService.getDeckForUser(userId, deckId);
                
                // Set attributes for presentation layer
                setUserAttribute(request);
                request.setAttribute("card", nextCard);
                request.setAttribute("deck", deck);
                request.setAttribute("cardIndex", nextIndex);
                request.setAttribute("totalCards", cards.size());
            }
            
        } catch (SQLException e) {
            logger.severe("Error recording answer: " + e.getMessage());
            request.setAttribute("error", "Failed to record answer");
        } catch (SecurityException e) {
            try {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            } catch (IOException ioException) {
                logger.severe("Failed to send error response: " + ioException.getMessage());
            }
        }
    }
    
    /**
     * Complete study session and show results
     */
    public StudySession completeStudySession(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        try {
            HttpSession httpSession = request.getSession(false);
            if (httpSession == null) {
                requireAuthentication(request, response);
                return null;
            }
            
            Long sessionId = (Long) httpSession.getAttribute("studySessionId");
            Long startTime = (Long) httpSession.getAttribute("studyStartTime");
            
            if (sessionId == null) {
                return null;
            }
            
            // Calculate duration
            int durationMinutes = 0;
            if (startTime != null) {
                long durationMillis = System.currentTimeMillis() - startTime;
                durationMinutes = (int) (durationMillis / 60000);
            }
            
            // Controller calls Service (domain layer)
            StudySession session = studyService.endStudySession(sessionId, durationMinutes);
            
            // Get deck info
            Long deckId = (Long) httpSession.getAttribute("studyDeckId");
            if (deckId == null && session != null) {
                deckId = session.getDeckId();
            }
            
            Deck deck = null;
            if (deckId != null) {
                try {
                    Long userId = getCurrentUserId(request);
                    deck = studyService.getDeckForUser(userId, deckId);
                } catch (SQLException | SecurityException e) {
                    logger.warning("Deck not found or access denied for deckId: " + deckId);
                    return null;
                }
            }
            
            // Clear study state from session
            httpSession.removeAttribute("studySessionId");
            httpSession.removeAttribute("studyDeckId");
            httpSession.removeAttribute("studyCards");
            httpSession.removeAttribute("studyCardIndex");
            httpSession.removeAttribute("studyStartTime");
            
            // Set attributes for presentation layer
            setUserAttribute(request);
            request.setAttribute("session", session);
            request.setAttribute("deck", deck);
            
            return session;
            
        } catch (SQLException e) {
            logger.severe("Error completing study session: " + e.getMessage());
            request.setAttribute("error", "Failed to complete study session");
            return null;
        }
    }
    
    /**
     * Get deck for user (helper method)
     */
    public Deck getDeckForUser(Long userId, Long deckId) throws SQLException {
        return studyService.getDeckForUser(userId, deckId);
    }
    
    /**
     * Get cards for deck (helper method)
     */
    public List<Card> getCardsForDeck(Long userId, Long deckId) throws SQLException {
        return studyService.getCardsForDeck(userId, deckId);
    }
}

