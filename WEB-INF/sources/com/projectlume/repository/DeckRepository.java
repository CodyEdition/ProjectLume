package com.projectlume.repository;

import com.projectlume.model.Deck;
import java.sql.SQLException;
import java.util.List;

/**
 * Repository interface for Deck entity
 * Defines the contract for deck data access operations
 */
public interface DeckRepository {
    
    /**
     * Creates a new deck
     * @param deck Deck object to create
     * @return Created deck with generated ID
     * @throws SQLException if database error occurs
     */
    Deck create(Deck deck) throws SQLException;
    
    /**
     * Finds deck by ID
     * @param id Deck ID
     * @return Deck object or null if not found
     * @throws SQLException if database error occurs
     */
    Deck findById(Long id) throws SQLException;
    
    /**
     * Finds decks by user ID
     * @param userId User ID
     * @return List of decks belonging to the user
     * @throws SQLException if database error occurs
     */
    List<Deck> findByUserId(Long userId) throws SQLException;
    
    /**
     * Updates deck information
     * @param deck Deck object with updated information
     * @return Updated deck object
     * @throws SQLException if database error occurs
     */
    Deck update(Deck deck) throws SQLException;
    
    /**
     * Soft deletes deck (marks as inactive)
     * @param id Deck ID to delete
     * @return true if deletion successful
     * @throws SQLException if database error occurs
     */
    boolean delete(Long id) throws SQLException;
    
    /**
     * Gets all active decks
     * @return List of all active decks
     * @throws SQLException if database error occurs
     */
    List<Deck> findAll() throws SQLException;
}
