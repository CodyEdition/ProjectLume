package com.projectlume.service;

import com.projectlume.dao.CardDAO;
import com.projectlume.dao.DeckDAO;
import com.projectlume.factory.DAOFactory;
import com.projectlume.model.Card;
import com.projectlume.model.Deck;
import com.projectlume.strategy.ValidationContext;

import java.sql.SQLException;
import java.util.List;

/**
 * Service for managing card-related operations
 * Handles business logic for card CRUD operations and ownership validation
 * Refactored to extend BaseService and use Validation Strategy pattern
 */
public class CardService extends BaseService {
    private final CardDAO cardDAO;
    private final DeckDAO deckDAO;
    private final ValidationContext validationContext;
    
    public CardService() {
        super();
        this.cardDAO = DAOFactory.createCardDAO();
        this.deckDAO = DAOFactory.createDeckDAO();
        this.validationContext = new ValidationContext();
    }
    
    /**
     * Get cards for a deck, validating that the user owns the deck
     * @param userId User ID
     * @param deckId Deck ID
     * @return List of cards belonging to the deck
     * @throws SQLException if database error occurs
     * @throws SecurityException if user doesn't own the deck
     */
    public List<Card> getCardsForDeck(Long userId, Long deckId) throws SQLException {
        // Validate deck ownership
        Deck deck = deckDAO.findById(deckId);
        if (deck == null) {
            throw new SQLException("Deck not found");
        }
        if (!deck.getUserId().equals(userId)) {
            throw new SecurityException("User does not own this deck");
        }
        
        return cardDAO.findByDeckId(deckId);
    }
    
    /**
     * Get a card by ID, validating that the user owns the deck containing the card
     * @param userId User ID
     * @param cardId Card ID
     * @return Card object
     * @throws SQLException if database error occurs
     * @throws SecurityException if user doesn't own the deck containing the card
     */
    public Card getCardForUser(Long userId, Long cardId) throws SQLException {
        Card card = cardDAO.findById(cardId);
        if (card == null) {
            throw new SQLException("Card not found");
        }
        
        // Validate deck ownership
        Deck deck = deckDAO.findById(card.getDeckId());
        if (deck == null) {
            throw new SQLException("Deck not found");
        }
        if (!deck.getUserId().equals(userId)) {
            throw new SecurityException("User does not own this deck");
        }
        
        return card;
    }
    
    /**
     * Get deck for a user, validating ownership
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
     * Create a new card, validating deck ownership and input
     * @param userId User ID
     * @param deckId Deck ID
     * @param frontText Front text of the card
     * @param backText Back text of the card
     * @param difficultyLevel Difficulty level (can be null, defaults to MEDIUM)
     * @return Created card object
     * @throws SQLException if database error occurs
     * @throws IllegalArgumentException if input validation fails
     * @throws SecurityException if user doesn't own the deck
     */
    public Card createCard(Long userId, Long deckId, String frontText, String backText, Card.DifficultyLevel difficultyLevel) 
            throws SQLException {
        // Validate input using Strategy pattern
        try {
            validationContext.validate("frontText", frontText, "frontText");
            validationContext.validate("backText", backText, "backText");
        } catch (com.projectlume.exception.ValidationException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        
        // Validate deck ownership
        Deck deck = deckDAO.findById(deckId);
        if (deck == null) {
            throw new SQLException("Deck not found");
        }
        if (!deck.getUserId().equals(userId)) {
            throw new SecurityException("User does not own this deck");
        }
        
        // Set default difficulty level if not provided
        Card.DifficultyLevel level = difficultyLevel != null ? difficultyLevel : Card.DifficultyLevel.MEDIUM;
        
        // Create card using template method from BaseService
        Card card = new Card(deckId, frontText.trim(), backText.trim(), level);
        Card createdCard = executeCreate(() -> cardDAO.create(card));
        
        logger.info("Card created successfully: " + frontText);
        return createdCard;
    }
    
    /**
     * Update a card, validating ownership and input
     * @param userId User ID
     * @param cardId Card ID
     * @param frontText Front text of the card
     * @param backText Back text of the card
     * @param difficultyLevel Difficulty level (can be null, keeps current)
     * @return Updated card object
     * @throws SQLException if database error occurs
     * @throws IllegalArgumentException if input validation fails
     * @throws SecurityException if user doesn't own the deck containing the card
     */
    public Card updateCard(Long userId, Long cardId, String frontText, String backText, Card.DifficultyLevel difficultyLevel) 
            throws SQLException {
        // Validate input using Strategy pattern
        try {
            validationContext.validate("frontText", frontText, "frontText");
            validationContext.validate("backText", backText, "backText");
        } catch (com.projectlume.exception.ValidationException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        
        // Get card and validate ownership
        Card card = getCardForUser(userId, cardId);
        
        // Update card fields using template method from BaseService
        card.setFrontText(frontText.trim());
        card.setBackText(backText.trim());
        if (difficultyLevel != null) {
            card.setDifficultyLevel(difficultyLevel);
        }
        
        Card updatedCard = executeUpdate(() -> cardDAO.update(card));
        logger.info("Card updated successfully: " + frontText);
        return updatedCard;
    }
    
    /**
     * Delete a card, validating ownership
     * @param userId User ID
     * @param cardId Card ID
     * @return true if deletion successful
     * @throws SQLException if database error occurs
     * @throws SecurityException if user doesn't own the deck containing the card
     */
    public boolean deleteCard(Long userId, Long cardId) throws SQLException {
        // Get card and validate ownership
        Card card = getCardForUser(userId, cardId);
        
        boolean deleted = executeDelete(() -> cardDAO.delete(cardId));
        if (deleted) {
            logger.info("Card deleted successfully: " + card.getFrontText());
        }
        return deleted;
    }
}

