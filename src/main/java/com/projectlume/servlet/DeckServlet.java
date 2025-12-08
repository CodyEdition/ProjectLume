package com.projectlume.servlet;

import com.projectlume.controller.DeckController;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet for deck management (CRUD operations)
 * Delegates to DeckController which manages communication between presentation, domain, and data layers
 */
@WebServlet(name = "DeckServlet", urlPatterns = {"/deck/*"})
public class DeckServlet extends HttpServlet {
    private final DeckController deckController;
    
    public DeckServlet() {
        this.deckController = new DeckController();
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
     * Delegates to DeckController which coordinates between layers
     */
    private void listDecks(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Delegate to controller (manages communication between presentation, domain, and data layers)
        deckController.listDecks(request, response);
        request.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(request, response);
    }
    
    /**
     * Create a new deck
     * Delegates to DeckController which coordinates between layers
     */
    private void createDeck(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String name = request.getParameter("name");
        String description = request.getParameter("description");
        
        // Delegate to controller (manages communication between presentation, domain, and data layers)
        String redirectUrl = deckController.createDeck(request, response, name, description);
        
        if (redirectUrl != null) {
            response.sendRedirect(redirectUrl);
        } else {
            request.getRequestDispatcher("/WEB-INF/views/deck-form.jsp").forward(request, response);
        }
    }
    
    /**
     * Show edit deck form
     * Delegates to DeckController which coordinates between layers
     */
    private void showEditForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            String deckIdStr = pathInfo.substring("/edit/".length());
            Long deckId = Long.parseLong(deckIdStr);
            
            // Delegate to controller (manages communication between presentation, domain, and data layers)
            deckController.showEditForm(request, response, deckId);
            request.getRequestDispatcher("/WEB-INF/views/deck-form.jsp").forward(request, response);
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
    
    /**
     * Update deck
     * Delegates to DeckController which coordinates between layers
     */
    private void updateDeck(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            String deckIdStr = pathInfo.substring("/edit/".length());
            Long deckId = Long.parseLong(deckIdStr);
            
            String name = request.getParameter("name");
            String description = request.getParameter("description");
            
            // Delegate to controller (manages communication between presentation, domain, and data layers)
            String redirectUrl = deckController.updateDeck(request, response, deckId, name, description);
            
            if (redirectUrl != null) {
                response.sendRedirect(redirectUrl);
            } else {
                // Try to reload deck for form
                try {
                    deckController.showEditForm(request, response, deckId);
                } catch (Exception ex) {
                    // Ignore - will show error only
                }
                request.getRequestDispatcher("/WEB-INF/views/deck-form.jsp").forward(request, response);
            }
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
    
    /**
     * Delete deck
     * Delegates to DeckController which coordinates between layers
     */
    private void deleteDeck(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            String deckIdStr = pathInfo.substring("/delete/".length());
            Long deckId = Long.parseLong(deckIdStr);
            
            // Delegate to controller (manages communication between presentation, domain, and data layers)
            String redirectUrl = deckController.deleteDeck(request, response, deckId);
            
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
        return deckController.isUserLoggedIn(request);
    }
    
    /**
     * Set user attribute in request for JSP pages
     */
    private void setUserAttribute(HttpServletRequest request) {
        deckController.setUserAttribute(request);
    }
}
