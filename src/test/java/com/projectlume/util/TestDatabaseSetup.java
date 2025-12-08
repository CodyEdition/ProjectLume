package com.projectlume.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

/**
 * Utility class
 */
public class TestDatabaseSetup {
    private static final Logger logger = Logger.getLogger(TestDatabaseSetup.class.getName());
    
    public static void initializeTestDatabase() throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {
            
            String createUsersTable = 
                "CREATE TABLE IF NOT EXISTS users (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(50) UNIQUE NOT NULL, " +
                "email VARCHAR(100) UNIQUE NOT NULL, " +
                "password_hash VARCHAR(255) NOT NULL, " +
                "first_name VARCHAR(50), " +
                "last_name VARCHAR(50), " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                "is_active BOOLEAN DEFAULT TRUE" +
                ")";
            
            String createDecksTable = 
                "CREATE TABLE IF NOT EXISTS decks (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "user_id BIGINT NOT NULL, " +
                "name VARCHAR(100) NOT NULL, " +
                "description TEXT, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                "is_active BOOLEAN DEFAULT TRUE, " +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                ")";
            
            String createCardsTable = 
                "CREATE TABLE IF NOT EXISTS cards (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "deck_id BIGINT NOT NULL, " +
                "front_text TEXT NOT NULL, " +
                "back_text TEXT NOT NULL, " +
                "difficulty_level VARCHAR(20) DEFAULT 'MEDIUM', " +
                "completed BOOLEAN DEFAULT FALSE, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                "is_active BOOLEAN DEFAULT TRUE, " +
                "FOREIGN KEY (deck_id) REFERENCES decks(id) ON DELETE CASCADE" +
                ")";
            
            String createStudySessionsTable = 
                "CREATE TABLE IF NOT EXISTS study_sessions (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "user_id BIGINT NOT NULL, " +
                "deck_id BIGINT NOT NULL, " +
                "session_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "cards_studied INT DEFAULT 0, " +
                "correct_answers INT DEFAULT 0, " +
                "incorrect_answers INT DEFAULT 0, " +
                "session_duration_minutes INT DEFAULT 0, " +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (deck_id) REFERENCES decks(id) ON DELETE CASCADE" +
                ")";
            
            String createCardStudyHistoryTable = 
                "CREATE TABLE IF NOT EXISTS card_study_history (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "card_id BIGINT NOT NULL, " +
                "user_id BIGINT NOT NULL, " +
                "was_correct BOOLEAN NOT NULL, " +
                "response_time_seconds INT, " +
                "study_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                ")";
            
            statement.execute(createUsersTable);
            statement.execute(createDecksTable);
            statement.execute(createCardsTable);
            statement.execute(createStudySessionsTable);
            statement.execute(createCardStudyHistoryTable);
            
            logger.info("Test database schema initialized successfully");
        }
    }
    
    public static void cleanupTestData() throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {
            
            // Delete in reverse order of foreign key dependencies
            statement.execute("DELETE FROM card_study_history");
            statement.execute("DELETE FROM study_sessions");
            statement.execute("DELETE FROM cards");
            statement.execute("DELETE FROM decks");
            statement.execute("DELETE FROM users");
            
            logger.info("Test data cleaned up successfully");
        }
    }
    
    public static void dropTestTables() throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {
            
            statement.execute("DROP TABLE IF EXISTS card_study_history");
            statement.execute("DROP TABLE IF EXISTS study_sessions");
            statement.execute("DROP TABLE IF EXISTS cards");
            statement.execute("DROP TABLE IF EXISTS decks");
            statement.execute("DROP TABLE IF EXISTS users");
            
            logger.info("Test tables dropped successfully");
        }
    }
}

