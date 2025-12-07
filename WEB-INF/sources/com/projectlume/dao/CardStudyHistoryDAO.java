package com.projectlume.dao;

import com.projectlume.model.CardStudyHistory;
import com.projectlume.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Data Access Object for CardStudyHistory entity
 * Handles all database operations related to card study history
 */
public class CardStudyHistoryDAO {
    private static final Logger logger = Logger.getLogger(CardStudyHistoryDAO.class.getName());
    
    // SQL queries
    private static final String INSERT_CARD_STUDY_HISTORY = 
        "INSERT INTO card_study_history (card_id, user_id, study_date, was_correct, response_time_seconds) " +
        "VALUES (?, ?, ?, ?, ?)";
    
    private static final String SELECT_CARD_STUDY_HISTORY_BY_CARD_ID = 
        "SELECT * FROM card_study_history WHERE card_id = ? ORDER BY study_date DESC";
    
    private static final String SELECT_CARD_STUDY_HISTORY_BY_USER_ID = 
        "SELECT * FROM card_study_history WHERE user_id = ? ORDER BY study_date DESC";
    
    private static final String SELECT_CARD_STATS = 
        "SELECT COUNT(*) as total_attempts, " +
        "SUM(CASE WHEN was_correct = true THEN 1 ELSE 0 END) as correct_attempts, " +
        "AVG(response_time_seconds) as avg_response_time " +
        "FROM card_study_history WHERE card_id = ? AND user_id = ?";
    
    /**
     * Create a new card study history entry
     * @param history CardStudyHistory object to create
     * @return Created card study history with generated ID
     * @throws SQLException if database error occurs
     */
    public CardStudyHistory create(CardStudyHistory history) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_CARD_STUDY_HISTORY, Statement.RETURN_GENERATED_KEYS)) {
            
            LocalDateTime now = LocalDateTime.now();
            statement.setLong(1, history.getCardId());
            statement.setLong(2, history.getUserId());
            statement.setTimestamp(3, Timestamp.valueOf(now));
            statement.setBoolean(4, history.isWasCorrect());
            if (history.getResponseTimeSeconds() != null) {
                statement.setInt(5, history.getResponseTimeSeconds());
            } else {
                statement.setNull(5, Types.INTEGER);
            }
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating card study history failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    history.setId(generatedKeys.getLong(1));
                    history.setStudyDate(now);
                } else {
                    throw new SQLException("Creating card study history failed, no ID obtained.");
                }
            }
            
            logger.info("Card study history created successfully: Card ID " + history.getCardId());
            return history;
        }
    }
    
    /**
     * Find card study history by card ID
     * @param cardId Card ID
     * @return List of card study history entries for the card
     * @throws SQLException if database error occurs
     */
    public List<CardStudyHistory> findByCardId(Long cardId) throws SQLException {
        List<CardStudyHistory> histories = new ArrayList<>();
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_CARD_STUDY_HISTORY_BY_CARD_ID)) {
            
            statement.setLong(1, cardId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    histories.add(mapResultSetToCardStudyHistory(resultSet));
                }
            }
        }
        
        return histories;
    }
    
    /**
     * Find card study history by user ID
     * @param userId User ID
     * @return List of card study history entries for the user
     * @throws SQLException if database error occurs
     */
    public List<CardStudyHistory> findByUserId(Long userId) throws SQLException {
        List<CardStudyHistory> histories = new ArrayList<>();
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_CARD_STUDY_HISTORY_BY_USER_ID)) {
            
            statement.setLong(1, userId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    histories.add(mapResultSetToCardStudyHistory(resultSet));
                }
            }
        }
        
        return histories;
    }
    
    /**
     * Get card statistics for a specific card and user
     * @param cardId Card ID
     * @param userId User ID
     * @return Map containing statistics: totalAttempts, correctAttempts, averageResponseTime
     * @throws SQLException if database error occurs
     */
    public Map<String, Object> getCardStats(Long cardId, Long userId) throws SQLException {
        Map<String, Object> stats = new HashMap<>();
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_CARD_STATS)) {
            
            statement.setLong(1, cardId);
            statement.setLong(2, userId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    stats.put("totalAttempts", resultSet.getLong("total_attempts"));
                    stats.put("correctAttempts", resultSet.getLong("correct_attempts"));
                    
                    double avgResponseTime = resultSet.getDouble("avg_response_time");
                    if (resultSet.wasNull()) {
                        stats.put("averageResponseTime", null);
                    } else {
                        stats.put("averageResponseTime", avgResponseTime);
                    }
                } else {
                    // No history found
                    stats.put("totalAttempts", 0L);
                    stats.put("correctAttempts", 0L);
                    stats.put("averageResponseTime", null);
                }
            }
        }
        
        return stats;
    }
    
    /**
     * Map ResultSet to CardStudyHistory object
     * @param resultSet Database result set
     * @return CardStudyHistory object
     * @throws SQLException if mapping fails
     */
    private CardStudyHistory mapResultSetToCardStudyHistory(ResultSet resultSet) throws SQLException {
        CardStudyHistory history = new CardStudyHistory();
        history.setId(resultSet.getLong("id"));
        history.setCardId(resultSet.getLong("card_id"));
        history.setUserId(resultSet.getLong("user_id"));
        
        Timestamp studyDate = resultSet.getTimestamp("study_date");
        if (studyDate != null) {
            history.setStudyDate(studyDate.toLocalDateTime());
        }
        
        history.setWasCorrect(resultSet.getBoolean("was_correct"));
        
        int responseTimeSeconds = resultSet.getInt("response_time_seconds");
        if (resultSet.wasNull()) {
            history.setResponseTimeSeconds(null);
        } else {
            history.setResponseTimeSeconds(responseTimeSeconds);
        }
        
        return history;
    }
}

