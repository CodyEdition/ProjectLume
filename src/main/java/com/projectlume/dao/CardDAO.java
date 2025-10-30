package com.projectlume.dao;

import com.projectlume.model.Card;
import com.projectlume.repository.CardRepository;
import com.projectlume.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Data Access Object for Card entity
 * Handles all database operations related to cards
 * Implements the Repository pattern for data access abstraction
 */
public class CardDAO implements CardRepository {
    private static final Logger logger = Logger.getLogger(CardDAO.class.getName());
    
    // SQL queries
    private static final String INSERT_CARD = 
        "INSERT INTO cards (deck_id, front_text, back_text, difficulty_level, completed, created_at, updated_at, is_active) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String SELECT_CARD_BY_ID = 
        "SELECT * FROM cards WHERE id = ? AND is_active = true";
    
    private static final String SELECT_CARDS_BY_DECK_ID = 
        "SELECT * FROM cards WHERE deck_id = ? AND is_active = true ORDER BY created_at DESC";
    
    
    
    private static final String UPDATE_CARD = 
        "UPDATE cards SET front_text = ?, back_text = ?, difficulty_level = ?, updated_at = ? WHERE id = ?";
    
    
    
    private static final String DELETE_CARD = 
        "UPDATE cards SET is_active = false, updated_at = ? WHERE id = ?";
    
    private static final String SELECT_ALL_CARDS = 
        "SELECT * FROM cards WHERE is_active = true ORDER BY created_at DESC";
    
    /**
     * Create a new card
     * @param card Card object to create
     * @return Created card with generated ID
     * @throws SQLException if database error occurs
     */
    public Card create(Card card) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_CARD, Statement.RETURN_GENERATED_KEYS)) {
            
            LocalDateTime now = LocalDateTime.now();
            statement.setLong(1, card.getDeckId());
            statement.setString(2, card.getFrontText());
            statement.setString(3, card.getBackText());
            statement.setString(4, card.getDifficultyLevel().name());
            statement.setBoolean(5, card.isCompleted());
            statement.setTimestamp(6, Timestamp.valueOf(now));
            statement.setTimestamp(7, Timestamp.valueOf(now));
            statement.setBoolean(8, card.isActive());
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating card failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    card.setId(generatedKeys.getLong(1));
                    card.setCreatedAt(now);
                    card.setUpdatedAt(now);
                } else {
                    throw new SQLException("Creating card failed, no ID obtained.");
                }
            }
            
            logger.info("Card created successfully: " + card.getFrontText());
            return card;
        }
    }
    
    /**
     * Find card by ID
     * @param id Card ID
     * @return Card object or null if not found
     * @throws SQLException if database error occurs
     */
    public Card findById(Long id) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_CARD_BY_ID)) {
            
            statement.setLong(1, id);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToCard(resultSet);
                }
            }
            
            return null;
        }
    }
    
    /**
     * Find cards by deck ID
     * @param deckId Deck ID
     * @return List of cards belonging to the deck
     * @throws SQLException if database error occurs
     */
    public List<Card> findByDeckId(Long deckId) throws SQLException {
        List<Card> cards = new ArrayList<>();
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_CARDS_BY_DECK_ID)) {
            
            statement.setLong(1, deckId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    cards.add(mapResultSetToCard(resultSet));
                }
            }
        }
        
        return cards;
    }
    
    /**
     * Update card information
     * @param card Card object with updated information
     * @return Updated card object
     * @throws SQLException if database error occurs
     */
    public Card update(Card card) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_CARD)) {
            
            LocalDateTime now = LocalDateTime.now();
            statement.setString(1, card.getFrontText());
            statement.setString(2, card.getBackText());
            statement.setString(3, card.getDifficultyLevel().name());
            statement.setTimestamp(4, Timestamp.valueOf(now));
            statement.setLong(5, card.getId());
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating card failed, no rows affected.");
            }
            
            card.setUpdatedAt(now);
            logger.info("Card updated successfully: " + card.getFrontText());
            return card;
        }
    }
    
    /**
     * Soft delete card (mark as inactive)
     * @param id Card ID to delete
     * @return true if deletion successful
     * @throws SQLException if database error occurs
     */
    public boolean delete(Long id) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_CARD)) {
            
            statement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            statement.setLong(2, id);
            
            int affectedRows = statement.executeUpdate();
            logger.info("Card deleted successfully: ID " + id);
            return affectedRows > 0;
        }
    }
    
    /**
     * Get all active cards
     * @return List of all active cards
     * @throws SQLException if database error occurs
     */
    public List<Card> findAll() throws SQLException {
        List<Card> cards = new ArrayList<>();
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL_CARDS);
             ResultSet resultSet = statement.executeQuery()) {
            
            while (resultSet.next()) {
                cards.add(mapResultSetToCard(resultSet));
            }
        }
        
        return cards;
    }
    
    
    
    
    
    /**
     * Map ResultSet to Card object
     * @param resultSet Database result set
     * @return Card object
     * @throws SQLException if mapping fails
     */
    private Card mapResultSetToCard(ResultSet resultSet) throws SQLException {
        Card card = new Card();
        card.setId(resultSet.getLong("id"));
        card.setDeckId(resultSet.getLong("deck_id"));
        card.setFrontText(resultSet.getString("front_text"));
        card.setBackText(resultSet.getString("back_text"));
        
        String difficultyLevelStr = resultSet.getString("difficulty_level");
        if (difficultyLevelStr != null) {
            card.setDifficultyLevel(Card.DifficultyLevel.valueOf(difficultyLevelStr));
        }
        
        card.setCompleted(resultSet.getBoolean("completed"));
        
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        if (createdAt != null) {
            card.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = resultSet.getTimestamp("updated_at");
        if (updatedAt != null) {
            card.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        card.setActive(resultSet.getBoolean("is_active"));
        
        return card;
    }
}
