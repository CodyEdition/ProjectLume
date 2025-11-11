package com.projectlume.servlet;

import com.projectlume.dao.CardDAO;
import com.projectlume.dao.DeckDAO;
import com.projectlume.factory.DAOFactory;
import com.projectlume.model.Card;
import com.projectlume.model.Deck;
import com.projectlume.model.StudySession;
import com.projectlume.model.User;
import com.projectlume.service.StudyService;

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
 * Servlet for handling study sessions
 */
@WebServlet(name = "StudyServlet", urlPatterns = {"/study/*"})
public class StudyServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(StudyServlet.class.getName());
    private final StudyService studyService;
    private final CardDAO cardDAO;
    private final DeckDAO deckDAO;
    
    public StudyServlet() {
        this.studyService = new StudyService();
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
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } else if (pathInfo.startsWith("/") && pathInfo.length() > 1) {
            // Extract deckId from path like /study/123
            String deckIdStr = pathInfo.substring(1);
            try {
                Long deckId = Long.parseLong(deckIdStr);
                startStudySession(request, response, deckId);
            } catch (NumberFormatException e) {
                // Check if it's /study/answer
                if (pathInfo.equals("/answer")) {
                    showCardAnswer(request, response);
                } else {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                }
            }
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
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } else if (pathInfo.equals("/answer")) {
            recordAnswer(request, response);
        } else if (pathInfo.equals("/complete")) {
            completeStudySession(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    /**
     * Start a study session for a deck
     */
    private void startStudySession(HttpServletRequest request, HttpServletResponse response, Long deckId) 
            throws ServletException, IOException {
        try {
            Long userId = getCurrentUserId(request);
            
            // Verify deck ownership
            Deck deck = deckDAO.findById(deckId);
            if (deck == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            if (!deck.getUserId().equals(userId)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            
            // Get cards for the deck
            List<Card> cards = cardDAO.findByDeckId(deckId);
            if (cards.isEmpty()) {
                request.setAttribute("error", "This deck has no cards to study");
                request.setAttribute("deck", deck);
                request.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(request, response);
                return;
            }
            
            // Start study session
            StudySession session = studyService.startStudySession(userId, deckId);
            
            // Store study state in session
            HttpSession httpSession = request.getSession();
            httpSession.setAttribute("studySessionId", session.getId());
            httpSession.setAttribute("studyDeckId", deckId);
            httpSession.setAttribute("studyCards", cards);
            httpSession.setAttribute("studyCardIndex", 0);
            httpSession.setAttribute("studyStartTime", System.currentTimeMillis());
            
            // Show first card
            Card currentCard = cards.get(0);
            request.setAttribute("card", currentCard);
            request.setAttribute("deck", deck);
            request.setAttribute("cardIndex", 0);
            request.setAttribute("totalCards", cards.size());
            request.getRequestDispatcher("/WEB-INF/views/study.jsp").forward(request, response);
            
        } catch (SQLException e) {
            logger.severe("Error starting study session: " + e.getMessage());
            request.setAttribute("error", "Failed to start study session");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
    
    /**
     * Show card answer (after flip)
     */
    private void showCardAnswer(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            HttpSession httpSession = request.getSession(false);
            if (httpSession == null) {
                response.sendRedirect(request.getContextPath() + "/auth");
                return;
            }
            
            Long sessionId = (Long) httpSession.getAttribute("studySessionId");
            Long deckId = (Long) httpSession.getAttribute("studyDeckId");
            @SuppressWarnings("unchecked")
            List<Card> cards = (List<Card>) httpSession.getAttribute("studyCards");
            Integer cardIndex = (Integer) httpSession.getAttribute("studyCardIndex");
            
            if (sessionId == null || deckId == null || cards == null || cardIndex == null) {
                response.sendRedirect(request.getContextPath() + "/dashboard");
                return;
            }
            
            if (cardIndex >= cards.size()) {
                // All cards studied, redirect to complete
                response.sendRedirect(request.getContextPath() + "/study/complete");
                return;
            }
            
            Card currentCard = cards.get(cardIndex);
            Deck deck = deckDAO.findById(deckId);
            
            request.setAttribute("card", currentCard);
            request.setAttribute("deck", deck);
            request.setAttribute("cardIndex", cardIndex);
            request.setAttribute("totalCards", cards.size());
            request.getRequestDispatcher("/WEB-INF/views/study.jsp").forward(request, response);
            
        } catch (SQLException e) {
            logger.severe("Error showing card answer: " + e.getMessage());
            request.setAttribute("error", "Failed to load card");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }
    
    /**
     * Record answer and move to next card
     */
    private void recordAnswer(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            HttpSession httpSession = request.getSession(false);
            if (httpSession == null) {
                response.sendRedirect(request.getContextPath() + "/auth");
                return;
            }
            
            Long sessionId = (Long) httpSession.getAttribute("studySessionId");
            Long userId = getCurrentUserId(request);
            @SuppressWarnings("unchecked")
            List<Card> cards = (List<Card>) httpSession.getAttribute("studyCards");
            Integer cardIndex = (Integer) httpSession.getAttribute("studyCardIndex");
            
            if (sessionId == null || cards == null || cardIndex == null) {
                response.sendRedirect(request.getContextPath() + "/dashboard");
                return;
            }
            
            if (cardIndex >= cards.size()) {
                response.sendRedirect(request.getContextPath() + "/study/complete");
                return;
            }
            
            Card currentCard = cards.get(cardIndex);
            String wasCorrectStr = request.getParameter("wasCorrect");
            boolean wasCorrect = "true".equals(wasCorrectStr);
            
            // Record answer
            studyService.recordCardAnswer(sessionId, currentCard.getId(), userId, wasCorrect, null);
            
            // Move to next card
            int nextIndex = cardIndex + 1;
            httpSession.setAttribute("studyCardIndex", nextIndex);
            
            if (nextIndex >= cards.size()) {
                // All cards studied, redirect to complete
                response.sendRedirect(request.getContextPath() + "/study/complete");
            } else {
                // Show next card front
                Card nextCard = cards.get(nextIndex);
                Long deckId = (Long) httpSession.getAttribute("studyDeckId");
                Deck deck = deckDAO.findById(deckId);
                
                request.setAttribute("card", nextCard);
                request.setAttribute("deck", deck);
                request.setAttribute("cardIndex", nextIndex);
                request.setAttribute("totalCards", cards.size());
                request.getRequestDispatcher("/WEB-INF/views/study.jsp").forward(request, response);
            }
            
        } catch (SQLException e) {
            logger.severe("Error recording answer: " + e.getMessage());
            request.setAttribute("error", "Failed to record answer");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }
    
    /**
     * Complete study session and show results
     */
    private void completeStudySession(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            HttpSession httpSession = request.getSession(false);
            if (httpSession == null) {
                response.sendRedirect(request.getContextPath() + "/auth");
                return;
            }
            
            Long sessionId = (Long) httpSession.getAttribute("studySessionId");
            Long startTime = (Long) httpSession.getAttribute("studyStartTime");
            
            if (sessionId == null) {
                response.sendRedirect(request.getContextPath() + "/dashboard");
                return;
            }
            
            // Calculate duration
            int durationMinutes = 0;
            if (startTime != null) {
                long durationMillis = System.currentTimeMillis() - startTime;
                durationMinutes = (int) (durationMillis / 60000); // Convert to minutes
            }
            
            // End session
            StudySession session = studyService.endStudySession(sessionId, durationMinutes);
            
            // Get deck info
            Long deckId = (Long) httpSession.getAttribute("studyDeckId");
            Deck deck = deckDAO.findById(deckId);
            
            // Clear study state from session
            httpSession.removeAttribute("studySessionId");
            httpSession.removeAttribute("studyDeckId");
            httpSession.removeAttribute("studyCards");
            httpSession.removeAttribute("studyCardIndex");
            httpSession.removeAttribute("studyStartTime");
            
            // Show results
            request.setAttribute("session", session);
            request.setAttribute("deck", deck);
            request.getRequestDispatcher("/WEB-INF/views/study-results.jsp").forward(request, response);
            
        } catch (SQLException e) {
            logger.severe("Error completing study session: " + e.getMessage());
            request.setAttribute("error", "Failed to complete study session");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
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
}

