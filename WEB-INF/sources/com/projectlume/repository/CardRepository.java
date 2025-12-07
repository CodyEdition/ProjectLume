package com.projectlume.repository;

import com.projectlume.model.Card;
import java.sql.SQLException;
import java.util.List;

/**
 * Repository interface for Card entity
 * Defines the contract for card data access operations
 */
public interface CardRepository {
    
    /**
     * Creates a new card
     * @param card Card object to create
     * @return Created card with generated ID
     * @throws SQLException if database error occurs
     */
    Card create(Card card) throws SQLException;
    
    /**
     * Finds card by ID
     * @param id Card ID
     * @return Card object or null if not found
     * @throws SQLException if database error occurs
     */
    Card findById(Long id) throws SQLException;
    
    /**
     * Finds cards by deck ID
     * @param deckId Deck ID
     * @return List of cards belonging to the deck
     * @throws SQLException if database error occurs
     */
    List<Card> findByDeckId(Long deckId) throws SQLException;
    
    
    /**
     * Updates card information
     * @param card Card object with updated information
     * @return Updated card object
     * @throws SQLException if database error occurs
     */
    Card update(Card card) throws SQLException;
    
    
    
    /**
     * Soft deletes card (marks as inactive)
     * @param id Card ID to delete
     * @return true if deletion successful
     * @throws SQLException if database error occurs
     */
    boolean delete(Long id) throws SQLException;
    
    /**
     * Gets all active cards
     * @return List of all active cards
     * @throws SQLException if database error occurs
     */
    List<Card> findAll() throws SQLException;
}
