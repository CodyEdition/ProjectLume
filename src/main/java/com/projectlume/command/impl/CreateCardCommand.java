package com.projectlume.command.impl;

import com.projectlume.command.Command;
import com.projectlume.command.CommandResult;
import com.projectlume.model.Card;
import com.projectlume.service.CardService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;

/**
 * Command for creating a new card
 */
public class CreateCardCommand implements Command {
    private final CardService cardService;
    private final Long userId;
    
    public CreateCardCommand(CardService cardService, Long userId) {
        this.cardService = cardService;
        this.userId = userId;
    }
    
    @Override
    public CommandResult execute(HttpServletRequest request, HttpServletResponse response) {
        try {
            String deckIdParam = request.getParameter("deckId");
            String frontText = request.getParameter("frontText");
            String backText = request.getParameter("backText");
            String difficultyLevelStr = request.getParameter("difficultyLevel");
            
            if (deckIdParam == null) {
                return CommandResult.error("Deck ID is required");
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
            
            // Create card via service
            cardService.createCard(userId, deckId, frontText, backText, difficultyLevel);
            
            return CommandResult.redirect(request.getContextPath() + "/card?deckId=" + deckId);
            
        } catch (NumberFormatException e) {
            return CommandResult.error("Invalid deck ID format");
        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
            return CommandResult.errorForward("/WEB-INF/views/card-form.jsp", e.getMessage());
        } catch (SQLException e) {
            request.setAttribute("error", "Failed to create card");
            return CommandResult.errorForward("/WEB-INF/views/card-form.jsp", "Failed to create card");
        } catch (SecurityException e) {
            return CommandResult.error("Access denied");
        }
    }
}

