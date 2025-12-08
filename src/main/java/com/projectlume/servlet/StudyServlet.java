package com.projectlume.servlet;

import com.projectlume.controller.StudyController;
import com.projectlume.model.Card;
import com.projectlume.model.Deck;
import com.projectlume.model.StudySession;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * Servlet for handling study sessions
 * Delegates to StudyController which manages communication between presentation, domain, and data layers
 */
@WebServlet(name = "StudyServlet", urlPatterns = {"/study/*"})
public class StudyServlet extends HttpServlet {
    private final StudyController studyController;
    
    public StudyServlet() {
        this.studyController = new StudyController();
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
                // Check if it's /study/answer or /study/complete
                if (pathInfo.equals("/answer")) {
                    showCardAnswer(request, response);
                } else if (pathInfo.equals("/complete")) {
                    completeStudySession(request, response);
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
     * Delegates to StudyController which coordinates between layers
     */
    private void startStudySession(HttpServletRequest request, HttpServletResponse response, Long deckId) 
            throws ServletException, IOException {
        // Delegate to controller (manages communication between presentation, domain, and data layers)
        StudySession session = studyController.startStudySession(request, response, deckId);
        
        if (session == null) {
            // Error occurred, check if we should redirect to dashboard
            HttpSession httpSession = request.getSession(false);
            if (httpSession != null && request.getAttribute("error") != null) {
                String error = (String) request.getAttribute("error");
                if ("This deck has no cards to study".equals(error)) {
                    request.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(request, response);
                    return;
                }
            }
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
            return;
        }
        
        // Get study state from session
        HttpSession httpSession = request.getSession();
        @SuppressWarnings("unchecked")
        List<Card> cards = (List<Card>) httpSession.getAttribute("studyCards");
        Deck deck = (Deck) request.getAttribute("deck");
        
        if (cards != null && !cards.isEmpty() && deck != null) {
            Card currentCard = cards.get(0);
            request.setAttribute("card", currentCard);
            request.setAttribute("deck", deck);
            request.setAttribute("cardIndex", 0);
            request.setAttribute("totalCards", cards.size());
        }
        
        request.getRequestDispatcher("/WEB-INF/views/study.jsp").forward(request, response);
    }
    
    /**
     * Show card answer (after flip)
     * Delegates to StudyController which coordinates between layers
     */
    private void showCardAnswer(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
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
        
        // Delegate to controller (manages communication between presentation, domain, and data layers)
        studyController.showCardAnswer(request, response);
        request.getRequestDispatcher("/WEB-INF/views/study.jsp").forward(request, response);
    }
    
    /**
     * Record answer and move to next card
     * Delegates to StudyController which coordinates between layers
     */
    private void recordAnswer(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession httpSession = request.getSession(false);
        if (httpSession == null) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }
        
        Long sessionId = (Long) httpSession.getAttribute("studySessionId");
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
        
        String wasCorrectStr = request.getParameter("wasCorrect");
        boolean wasCorrect = "true".equals(wasCorrectStr);
        
        // Delegate to controller (manages communication between presentation, domain, and data layers)
        studyController.recordAnswer(request, response, wasCorrect);
        
        // Check if we should redirect to complete or show next card
        Integer nextIndex = (Integer) httpSession.getAttribute("studyCardIndex");
        if (nextIndex != null && nextIndex >= cards.size()) {
            // All cards studied, redirect to complete
            response.sendRedirect(request.getContextPath() + "/study/complete");
        } else {
            // Show next card front
            request.getRequestDispatcher("/WEB-INF/views/study.jsp").forward(request, response);
        }
    }
    
    /**
     * Complete study session and show results
     * Delegates to StudyController which coordinates between layers
     */
    private void completeStudySession(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession httpSession = request.getSession(false);
        if (httpSession == null) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }
        
        Long sessionId = (Long) httpSession.getAttribute("studySessionId");
        if (sessionId == null) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }
        
        // Delegate to controller (manages communication between presentation, domain, and data layers)
        StudySession session = studyController.completeStudySession(request, response);
        
        if (session == null) {
            // Error occurred or deck not found
            Deck deck = (Deck) request.getAttribute("deck");
            if (deck == null) {
                response.sendRedirect(request.getContextPath() + "/dashboard");
                return;
            }
        }
        
        // Show results
        request.getRequestDispatcher("/WEB-INF/views/study-results.jsp").forward(request, response);
    }
    
    /**
     * Check if user is logged in
     */
    private boolean isUserLoggedIn(HttpServletRequest request) {
        return studyController.isUserLoggedIn(request);
    }
}

