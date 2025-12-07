package com.projectlume.service;

import com.projectlume.builder.DeckStatsDTOBuilder;
import com.projectlume.dao.DeckDAO;
import com.projectlume.dto.DeckStatsDTO;
import com.projectlume.factory.DAOFactory;
import com.projectlume.model.Deck;
import com.projectlume.strategy.ValidationContext;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing deck-related operations and statistics
 * Refactored to extend BaseService and use Builder pattern for DTOs
 */
public class DeckService extends BaseService {
    private final DeckDAO deckDAO;
    private final ValidationContext validationContext;
    
    public DeckService() {
        super();
        this.deckDAO = DAOFactory.createDeckDAO();
        this.validationContext = new ValidationContext();
    }
    
    /**
     * Get deck statistics for all decks belonging to a user
     * Aggregates data from DeckDAO, CardDAO, and StudySessionDAO to create DeckStatsDTO objects
     * 
     * @param userId User ID
     * @return List of DeckStatsDTO objects containing statistics for each deck
     * @throws SQLException if database error occurs
     */
    public List<DeckStatsDTO> getDeckStatisticsForUser(Long userId) throws SQLException {
        List<DeckStatsDTO> deckStatsList = new ArrayList<>();

        // Get all decks owned by the user
        List<Deck> decks = deckDAO.findByUserId(userId);

        for (Deck deck : decks) {
            Long deckId = deck.getId();

            // Use the optimized DeckDAO statistics methods
            int totalCards = deckDAO.getCardCountForDeck(deckId);
            int cardsStudied = deckDAO.getStudiedCardCountForDeck(deckId, userId);
            LocalDateTime lastStudyDate = deckDAO.getLastStudyDateForDeck(deckId, userId);
            Double averageAccuracy = deckDAO.getAccuracyForDeck(deckId, userId);

            // Calculate completion percentage
            double completionPercentage =
                (totalCards == 0) ? 0.0 : (cardsStudied / (double) totalCards) * 100.0;

            // Create DeckStatsDTO using Builder pattern
            DeckStatsDTO deckStats = DeckStatsDTOBuilder.builder()
                    .deckId(deckId)
                    .name(deck.getName())
                    .description(deck.getDescription())
                    .totalCards(totalCards)
                    .cardsStudied(cardsStudied)
                    .completionPercentage(completionPercentage)
                    .createdAt(deck.getCreatedAt())
                    .lastStudyDate(lastStudyDate)
                    .averageAccuracy(averageAccuracy)
                    .build();

            deckStatsList.add(deckStats);
        }

        logger.info("Retrieved statistics for " + deckStatsList.size() + " decks for user " + userId);
        return executeRead(() -> deckStatsList);
    }

    /**
     * Get a deck by ID, validating that the user owns it
     * @param userId User ID
     * @param deckId Deck ID
     * @return Deck object
     * @throws SQLException if database error occurs
     * @throws SecurityException if user doesn't own the deck
     */
    public Deck getDeckForUser(Long userId, Long deckId) throws SQLException {
        Deck deck = deckDAO.findById(deckId);
        if (deck == null) {
            throw new SQLException("Deck not found");
        }
        if (!deck.getUserId().equals(userId)) {
            throw new SecurityException("User does not own this deck");
        }
        return deck;
    }
    
    /**
     * Get all decks for a user
     * @param userId User ID
     * @return List of decks belonging to the user
     * @throws SQLException if database error occurs
     */
    public List<Deck> getDecksForUser(Long userId) throws SQLException {
        return deckDAO.findByUserId(userId);
    }
    
    /**
     * Create a new deck, validating input
     * @param userId User ID
     * @param name Deck name
     * @param description Deck description (can be null)
     * @return Created deck object
     * @throws SQLException if database error occurs
     * @throws IllegalArgumentException if input validation fails
     */
    public Deck createDeck(Long userId, String name, String description) throws SQLException {
        // Validate input using Strategy pattern
        try {
            validationContext.validate("deckName", name, "deckName");
        } catch (com.projectlume.exception.ValidationException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        
        // Create deck using template method from BaseService
        Deck deck = new Deck(userId, name.trim(), description != null ? description.trim() : null);
        Deck createdDeck = executeCreate(() -> deckDAO.create(deck));
        
        logger.info("Deck created successfully: " + name);
        return createdDeck;
    }
    
    /**
     * Update a deck, validating ownership and input
     * @param userId User ID
     * @param deckId Deck ID
     * @param name Deck name
     * @param description Deck description (can be null)
     * @return Updated deck object
     * @throws SQLException if database error occurs
     * @throws IllegalArgumentException if input validation fails
     * @throws SecurityException if user doesn't own the deck
     */
    public Deck updateDeck(Long userId, Long deckId, String name, String description) throws SQLException {
        // Validate input using Strategy pattern
        try {
            validationContext.validate("deckName", name, "deckName");
        } catch (com.projectlume.exception.ValidationException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        
        // Get deck and validate ownership
        Deck deck = getDeckForUser(userId, deckId);
        
        // Update deck fields using template method from BaseService
        deck.setName(name.trim());
        deck.setDescription(description != null ? description.trim() : null);
        
        Deck updatedDeck = executeUpdate(() -> deckDAO.update(deck));
        logger.info("Deck updated successfully: " + name);
        return updatedDeck;
    }
    
    /**
     * Delete a deck, validating ownership
     * @param userId User ID
     * @param deckId Deck ID
     * @return true if deletion successful
     * @throws SQLException if database error occurs
     * @throws SecurityException if user doesn't own the deck
     */
    public boolean deleteDeck(Long userId, Long deckId) throws SQLException {
        Deck deck = getDeckForUser(userId, deckId);
        boolean deleted = executeDelete(() -> deckDAO.delete(deckId));
        if (deleted) {
            logger.info("Deck deleted successfully: " + deck.getName());
        }
        return deleted;
    }
}

