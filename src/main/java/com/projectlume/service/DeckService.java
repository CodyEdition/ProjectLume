package com.projectlume.service;

import com.projectlume.dao.CardDAO;
import com.projectlume.dao.CardStudyHistoryDAO;
import com.projectlume.dao.DeckDAO;
import com.projectlume.dao.StudySessionDAO;
import com.projectlume.dto.DeckStatsDTO;
import com.projectlume.factory.DAOFactory;
import com.projectlume.model.Card;
import com.projectlume.model.CardStudyHistory;
import com.projectlume.model.Deck;
import com.projectlume.model.StudySession;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Service for managing deck-related operations and statistics
 */
public class DeckService {
    private static final Logger logger = Logger.getLogger(DeckService.class.getName());
    private final DeckDAO deckDAO;
    private final CardDAO cardDAO;
    private final StudySessionDAO studySessionDAO;
    private final CardStudyHistoryDAO cardStudyHistoryDAO;
    
    public DeckService() {
        this.deckDAO = DAOFactory.createDeckDAO();
        this.cardDAO = DAOFactory.createCardDAO();
        this.studySessionDAO = DAOFactory.createStudySessionDAO();
        this.cardStudyHistoryDAO = DAOFactory.createCardStudyHistoryDAO();
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
        
        // Get all decks for the user
        List<Deck> decks = deckDAO.findByUserId(userId);
        
        // For each deck, calculate statistics
        for (Deck deck : decks) {
            Long deckId = deck.getId();
            
            // Get total card count
            List<Card> cards = cardDAO.findByDeckId(deckId);
            int totalCards = cards.size();
            
            // Get study sessions for this deck
            List<StudySession> sessions = studySessionDAO.findByDeckId(deckId);
            
            // Get unique cards studied from card_study_history
            Set<Long> uniqueCardsStudied = new HashSet<>();
            int totalCorrectAnswers = 0;
            int totalAttempts = 0;
            LocalDateTime lastStudyDate = null;
            
            for (Card card : cards) {
                List<CardStudyHistory> history = cardStudyHistoryDAO.findByCardId(card.getId());
                for (CardStudyHistory entry : history) {
                    if (entry.getUserId().equals(userId)) {
                        uniqueCardsStudied.add(card.getId());
                        totalAttempts++;
                        if (entry.isWasCorrect()) {
                            totalCorrectAnswers++;
                        }
                        LocalDateTime entryDate = entry.getStudyDate();
                        if (entryDate != null && (lastStudyDate == null || entryDate.isAfter(lastStudyDate))) {
                            lastStudyDate = entryDate;
                        }
                    }
                }
            }
            
            int cardsStudied = uniqueCardsStudied.size();
            
            // Calculate completion percentage based on unique cards
            double completionPercentage = 0.0;
            if (totalCards > 0) {
                completionPercentage = (double) cardsStudied / totalCards * 100.0;
            }
            
            // Use session date if no card history exists
            if (lastStudyDate == null && !sessions.isEmpty()) {
                lastStudyDate = sessions.stream()
                        .map(StudySession::getSessionDate)
                        .max(Comparator.naturalOrder())
                        .orElse(null);
            }
            
            // Calculate average accuracy based on all attempts
            Double averageAccuracy = null;
            if (totalAttempts > 0) {
                averageAccuracy = (double) totalCorrectAnswers / totalAttempts * 100.0;
            }
            
            // Create DeckStatsDTO
            DeckStatsDTO deckStats = new DeckStatsDTO(
                    deckId,
                    deck.getName(),
                    deck.getDescription(),
                    totalCards,
                    cardsStudied,
                    completionPercentage,
                    deck.getCreatedAt(),
                    lastStudyDate,
                    averageAccuracy
            );
            
            deckStatsList.add(deckStats);
        }
        
        logger.info("Retrieved statistics for " + deckStatsList.size() + " decks for user " + userId);
        return deckStatsList;
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
        // Validate input
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Deck name is required");
        }
        
        // Create deck
        Deck deck = new Deck(userId, name.trim(), description != null ? description.trim() : null);
        Deck createdDeck = deckDAO.create(deck);
        
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
        // Validate input
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Deck name is required");
        }
        
        // Get deck and validate ownership
        Deck deck = getDeckForUser(userId, deckId);
        
        // Update deck fields
        deck.setName(name.trim());
        deck.setDescription(description != null ? description.trim() : null);
        
        Deck updatedDeck = deckDAO.update(deck);
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
        boolean deleted = deckDAO.delete(deckId);
        if (deleted) {
            logger.info("Deck deleted successfully: " + deck.getName());
        }
        return deleted;
    }
}

