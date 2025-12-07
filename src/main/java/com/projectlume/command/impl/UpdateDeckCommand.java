package com.projectlume.command.impl;

import com.projectlume.command.Command;
import com.projectlume.command.CommandResult;
import com.projectlume.service.DeckService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;

/**
 * Command for updating a deck
 */
public class UpdateDeckCommand implements Command {
    private final DeckService deckService;
    private final Long userId;
    private final Long deckId;
    
    public UpdateDeckCommand(DeckService deckService, Long userId, Long deckId) {
        this.deckService = deckService;
        this.userId = userId;
        this.deckId = deckId;
    }
    
    @Override
    public CommandResult execute(HttpServletRequest request, HttpServletResponse response) {
        try {
            String name = request.getParameter("name");
            String description = request.getParameter("description");
            
            // Update deck via service
            deckService.updateDeck(userId, deckId, name, description);
            
            return CommandResult.redirect(request.getContextPath() + "/deck");
            
        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
            // Try to reload deck for form
            try {
                var deck = deckService.getDeckForUser(userId, deckId);
                request.setAttribute("deck", deck);
            } catch (Exception ex) {
                // Ignore - will show error only
            }
            return CommandResult.errorForward("/WEB-INF/views/deck-form.jsp", e.getMessage());
        } catch (SQLException e) {
            request.setAttribute("error", "Failed to update deck");
            return CommandResult.errorForward("/WEB-INF/views/deck-form.jsp", "Failed to update deck");
        } catch (SecurityException e) {
            return CommandResult.error("Access denied");
        }
    }
}

