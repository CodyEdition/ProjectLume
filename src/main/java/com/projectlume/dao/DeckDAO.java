package com.projectlume.dao;

import com.projectlume.model.Deck;
import com.projectlume.repository.DeckRepository;
import com.projectlume.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Data Access Object for Deck entity
 * Handles all database operations related to decks
 * Implements the Repository pattern for data access abstraction
 */
public class DeckDAO implements DeckRepository {
    private static final Logger logger = Logger.getLogger(DeckDAO.class.getName());
    
    // SQL queries
    private static final String INSERT_DECK = 
        "INSERT INTO decks (user_id, name, description, created_at, updated_at, is_active) " +
        "VALUES (?, ?, ?, ?, ?, ?)";
    
    private static final String SELECT_DECK_BY_ID = 
        "SELECT * FROM decks WHERE id = ? AND is_active = true";
    
    private static final String SELECT_DECKS_BY_USER_ID = 
        "SELECT * FROM decks WHERE user_id = ? AND is_active = true ORDER BY created_at DESC";
    
    private static final String UPDATE_DECK = 
        "UPDATE decks SET name = ?, description = ?, updated_at = ? WHERE id = ?";
    
    private static final String DELETE_DECK = 
        "UPDATE decks SET is_active = false, updated_at = ? WHERE id = ?";
    
    private static final String SELECT_ALL_DECKS = 
        "SELECT * FROM decks WHERE is_active = true ORDER BY created_at DESC";
    
    private static final String COUNT_CARDS_FOR_DECK = 
        "SELECT COUNT(*) FROM cards WHERE deck_id = ? AND is_active = true";
    
    private static final String COUNT_STUDIED_CARDS_FOR_DECK = 
        "SELECT COUNT(DISTINCT csh.card_id) " +
        "FROM card_study_history csh " +
        "INNER JOIN cards c ON csh.card_id = c.id " +
        "WHERE c.deck_id = ? AND csh.user_id = ? AND c.is_active = true";
    
    private static final String GET_LAST_STUDY_DATE_FOR_DECK = 
        "SELECT MAX(csh.study_date) as last_study_date " +
        "FROM card_study_history csh " +
        "INNER JOIN cards c ON csh.card_id = c.id " +
        "WHERE c.deck_id = ? AND csh.user_id = ? AND c.is_active = true";
    
    private static final String GET_ACCURACY_FOR_DECK = 
        "SELECT " +
        "  COUNT(*) as total_attempts, " +
        "  SUM(CASE WHEN csh.was_correct = true THEN 1 ELSE 0 END) as correct_attempts " +
        "FROM card_study_history csh " +
        "INNER JOIN cards c ON csh.card_id = c.id " +
        "WHERE c.deck_id = ? AND csh.user_id = ? AND c.is_active = true";
    
    private static final String GET_LAST_STUDY_DATE_FROM_SESSIONS = 
        "SELECT MAX(session_date) as last_study_date " +
        "FROM study_sessions " +
        "WHERE deck_id = ? AND user_id = ?";
    
    /**
     * Create a new deck
     * @param deck Deck object to create
     * @return Created deck with generated ID
     * @throws SQLException if database error occurs
     */
    public Deck create(Deck deck) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_DECK, Statement.RETURN_GENERATED_KEYS)) {
            
            LocalDateTime now = LocalDateTime.now();
            statement.setLong(1, deck.getUserId());
            statement.setString(2, deck.getName());
            statement.setString(3, deck.getDescription());
            statement.setTimestamp(4, Timestamp.valueOf(now));
            statement.setTimestamp(5, Timestamp.valueOf(now));
            statement.setBoolean(6, deck.isActive());
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating deck failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    deck.setId(generatedKeys.getLong(1));
                    deck.setCreatedAt(now);
                    deck.setUpdatedAt(now);
                } else {
                    throw new SQLException("Creating deck failed, no ID obtained.");
                }
            }
            
            logger.info("Deck created successfully: " + deck.getName());
            return deck;
        }
    }
    
    /**
     * Find deck by ID
     * @param id Deck ID
     * @return Deck object or null if not found
     * @throws SQLException if database error occurs
     */
    public Deck findById(Long id) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_DECK_BY_ID)) {
            
            statement.setLong(1, id);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToDeck(resultSet);
                }
            }
            
            return null;
        }
    }
    
    /**
     * Find decks by user ID
     * @param userId User ID
     * @return List of decks belonging to the user
     * @throws SQLException if database error occurs
     */
    public List<Deck> findByUserId(Long userId) throws SQLException {
        List<Deck> decks = new ArrayList<>();
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_DECKS_BY_USER_ID)) {
            
            statement.setLong(1, userId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    decks.add(mapResultSetToDeck(resultSet));
                }
            }
        }
        
        return decks;
    }
    
    /**
     * Update deck information
     * @param deck Deck object with updated information
     * @return Updated deck object
     * @throws SQLException if database error occurs
     */
    public Deck update(Deck deck) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_DECK)) {
            
            LocalDateTime now = LocalDateTime.now();
            statement.setString(1, deck.getName());
            statement.setString(2, deck.getDescription());
            statement.setTimestamp(3, Timestamp.valueOf(now));
            statement.setLong(4, deck.getId());
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating deck failed, no rows affected.");
            }
            
            deck.setUpdatedAt(now);
            logger.info("Deck updated successfully: " + deck.getName());
            return deck;
        }
    }
    
    /**
     * Soft delete deck (mark as inactive)
     * @param id Deck ID to delete
     * @return true if deletion successful
     * @throws SQLException if database error occurs
     */
    public boolean delete(Long id) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_DECK)) {
            
            statement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            statement.setLong(2, id);
            
            int affectedRows = statement.executeUpdate();
            logger.info("Deck deleted successfully: ID " + id);
            return affectedRows > 0;
        }
    }
    
    /**
     * Get all active decks
     * @return List of all active decks
     * @throws SQLException if database error occurs
     */
    public List<Deck> findAll() throws SQLException {
        List<Deck> decks = new ArrayList<>();
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL_DECKS);
             ResultSet resultSet = statement.executeQuery()) {
            
            while (resultSet.next()) {
                decks.add(mapResultSetToDeck(resultSet));
            }
        }
        
        return decks;
    }
    
    /**
     * Get the count of cards in a deck
     * @param deckId Deck ID
     * @return Number of active cards in the deck
     * @throws SQLException if database error occurs
     */
    public int getCardCountForDeck(Long deckId) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(COUNT_CARDS_FOR_DECK)) {
            
            statement.setLong(1, deckId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
            
            return 0;
        }
    }
    
    /**
     * Get the count of distinct cards that have been studied by a user in a deck
     * @param deckId Deck ID
     * @param userId User ID
     * @return Number of distinct cards studied by the user in the deck
     * @throws SQLException if database error occurs
     */
    public int getStudiedCardCountForDeck(Long deckId, Long userId) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(COUNT_STUDIED_CARDS_FOR_DECK)) {
            
            statement.setLong(1, deckId);
            statement.setLong(2, userId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
            
            return 0;
        }
    }
    
    /**
     * Get the last study date for a deck by a user
     * @param deckId Deck ID
     * @param userId User ID
     * @return Last study date, or null if no study history exists
     * @throws SQLException if database error occurs
     */
    public LocalDateTime getLastStudyDateForDeck(Long deckId, Long userId) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_LAST_STUDY_DATE_FOR_DECK)) {
            
            statement.setLong(1, deckId);
            statement.setLong(2, userId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Timestamp lastStudyDate = resultSet.getTimestamp("last_study_date");
                    if (lastStudyDate != null && !resultSet.wasNull()) {
                        return lastStudyDate.toLocalDateTime();
                    }
                }
            }
        }
        
        // Fallback
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_LAST_STUDY_DATE_FROM_SESSIONS)) {
            
            statement.setLong(1, deckId);
            statement.setLong(2, userId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Timestamp lastStudyDate = resultSet.getTimestamp("last_study_date");
                    if (lastStudyDate != null && !resultSet.wasNull()) {
                        return lastStudyDate.toLocalDateTime();
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * Get the average accuracy for a deck by a user
     * Accuracy is calculated as: (correct_attempts / total_attempts) * 100
     * @param deckId Deck ID
     * @param userId User ID
     * @return Average accuracy as a percentage (0.0 to 100.0), or null if no attempts exist
     * @throws SQLException if database error occurs
     */
    public Double getAccuracyForDeck(Long deckId, Long userId) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_ACCURACY_FOR_DECK)) {
            
            statement.setLong(1, deckId);
            statement.setLong(2, userId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    long totalAttempts = resultSet.getLong("total_attempts");
                    long correctAttempts = resultSet.getLong("correct_attempts");
                    
                    if (totalAttempts == 0) {
                        return null;
                    }
                    
                    return ((double) correctAttempts / totalAttempts) * 100.0;
                }
            }
            
            return null;
        }
    }
    
    /**
     * Map ResultSet to Deck object
     * @param resultSet Database result set
     * @return Deck object
     * @throws SQLException if mapping fails
     */
    private Deck mapResultSetToDeck(ResultSet resultSet) throws SQLException {
        Deck deck = new Deck();
        deck.setId(resultSet.getLong("id"));
        deck.setUserId(resultSet.getLong("user_id"));
        deck.setName(resultSet.getString("name"));
        deck.setDescription(resultSet.getString("description"));
        
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        if (createdAt != null) {
            deck.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = resultSet.getTimestamp("updated_at");
        if (updatedAt != null) {
            deck.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        deck.setActive(resultSet.getBoolean("is_active"));
        
        return deck;
    }
}