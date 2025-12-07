package com.projectlume.dao;

import com.projectlume.model.StudySession;
import com.projectlume.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Data Access Object for StudySession entity
 * Handles all database operations related to study sessions
 */
public class StudySessionDAO {
    private static final Logger logger = Logger.getLogger(StudySessionDAO.class.getName());
    
    // SQL queries
    private static final String INSERT_STUDY_SESSION = 
        "INSERT INTO study_sessions (user_id, deck_id, session_date, cards_studied, correct_answers, incorrect_answers, session_duration_minutes) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?)";
    
    private static final String SELECT_STUDY_SESSION_BY_ID = 
        "SELECT * FROM study_sessions WHERE id = ?";
    
    private static final String SELECT_STUDY_SESSIONS_BY_USER_ID = 
        "SELECT * FROM study_sessions WHERE user_id = ? ORDER BY session_date DESC";
    
    private static final String SELECT_STUDY_SESSIONS_BY_DECK_ID = 
        "SELECT * FROM study_sessions WHERE deck_id = ? ORDER BY session_date DESC";
    
    private static final String UPDATE_STUDY_SESSION = 
        "UPDATE study_sessions SET user_id = ?, deck_id = ?, session_date = ?, cards_studied = ?, correct_answers = ?, incorrect_answers = ?, session_duration_minutes = ? WHERE id = ?";
    
    /**
     * Create a new study session
     * @param session StudySession object to create
     * @return Created study session with generated ID
     * @throws SQLException if database error occurs
     */
    public StudySession create(StudySession session) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_STUDY_SESSION, Statement.RETURN_GENERATED_KEYS)) {
            
            LocalDateTime now = LocalDateTime.now();
            statement.setLong(1, session.getUserId());
            statement.setLong(2, session.getDeckId());
            statement.setTimestamp(3, Timestamp.valueOf(now));
            statement.setInt(4, session.getCardsStudied());
            statement.setInt(5, session.getCorrectAnswers());
            statement.setInt(6, session.getIncorrectAnswers());
            statement.setInt(7, session.getSessionDurationMinutes());
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating study session failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    session.setId(generatedKeys.getLong(1));
                    session.setSessionDate(now);
                } else {
                    throw new SQLException("Creating study session failed, no ID obtained.");
                }
            }
            
            logger.info("Study session created successfully: ID " + session.getId());
            return session;
        }
    }
    
    /**
     * Find study session by ID
     * @param id Study session ID
     * @return StudySession object or null if not found
     * @throws SQLException if database error occurs
     */
    public StudySession findById(Long id) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_STUDY_SESSION_BY_ID)) {
            
            statement.setLong(1, id);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToStudySession(resultSet);
                }
            }
            
            return null;
        }
    }
    
    /**
     * Find study sessions by user ID
     * @param userId User ID
     * @return List of study sessions belonging to the user
     * @throws SQLException if database error occurs
     */
    public List<StudySession> findByUserId(Long userId) throws SQLException {
        List<StudySession> sessions = new ArrayList<>();
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_STUDY_SESSIONS_BY_USER_ID)) {
            
            statement.setLong(1, userId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    sessions.add(mapResultSetToStudySession(resultSet));
                }
            }
        }
        
        return sessions;
    }
    
    /**
     * Find study sessions by deck ID
     * @param deckId Deck ID
     * @return List of study sessions for the deck
     * @throws SQLException if database error occurs
     */
    public List<StudySession> findByDeckId(Long deckId) throws SQLException {
        List<StudySession> sessions = new ArrayList<>();
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_STUDY_SESSIONS_BY_DECK_ID)) {
            
            statement.setLong(1, deckId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    sessions.add(mapResultSetToStudySession(resultSet));
                }
            }
        }
        
        return sessions;
    }
    
    /**
     * Update study session information
     * @param session StudySession object with updated information
     * @return Updated study session object
     * @throws SQLException if database error occurs
     */
    public StudySession update(StudySession session) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_STUDY_SESSION)) {
            
            statement.setLong(1, session.getUserId());
            statement.setLong(2, session.getDeckId());
            statement.setTimestamp(3, Timestamp.valueOf(session.getSessionDate()));
            statement.setInt(4, session.getCardsStudied());
            statement.setInt(5, session.getCorrectAnswers());
            statement.setInt(6, session.getIncorrectAnswers());
            statement.setInt(7, session.getSessionDurationMinutes());
            statement.setLong(8, session.getId());
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating study session failed, no rows affected.");
            }
            
            logger.info("Study session updated successfully: ID " + session.getId());
            return session;
        }
    }
    
    /**
     * Map ResultSet to StudySession object
     * @param resultSet Database result set
     * @return StudySession object
     * @throws SQLException if mapping fails
     */
    private StudySession mapResultSetToStudySession(ResultSet resultSet) throws SQLException {
        StudySession session = new StudySession();
        session.setId(resultSet.getLong("id"));
        session.setUserId(resultSet.getLong("user_id"));
        session.setDeckId(resultSet.getLong("deck_id"));
        
        Timestamp sessionDate = resultSet.getTimestamp("session_date");
        if (sessionDate != null) {
            session.setSessionDate(sessionDate.toLocalDateTime());
        }
        
        session.setCardsStudied(resultSet.getInt("cards_studied"));
        session.setCorrectAnswers(resultSet.getInt("correct_answers"));
        session.setIncorrectAnswers(resultSet.getInt("incorrect_answers"));
        session.setSessionDurationMinutes(resultSet.getInt("session_duration_minutes"));
        
        return session;
    }
}

