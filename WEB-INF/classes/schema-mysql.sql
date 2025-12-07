-- Reset database
DROP DATABASE IF EXISTS projectlume;
-- Create database
CREATE DATABASE IF NOT EXISTS projectlume;
USE projectlume;

-- Users table for authentication
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

-- Decks table for organizing flashcards
CREATE TABLE IF NOT EXISTS decks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Cards table for individual flashcards
CREATE TABLE IF NOT EXISTS cards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    deck_id BIGINT NOT NULL,
    front_text TEXT NOT NULL,
    back_text TEXT NOT NULL,
    difficulty_level ENUM('EASY', 'MEDIUM', 'HARD') DEFAULT 'MEDIUM',
    completed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (deck_id) REFERENCES decks(id) ON DELETE CASCADE
);

-- Study sessions table for tracking user progress
CREATE TABLE IF NOT EXISTS study_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    deck_id BIGINT NOT NULL,
    session_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    cards_studied INT DEFAULT 0,
    correct_answers INT DEFAULT 0,
    incorrect_answers INT DEFAULT 0,
    session_duration_minutes INT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (deck_id) REFERENCES decks(id) ON DELETE CASCADE
);

-- Card study history for spaced repetition
CREATE TABLE IF NOT EXISTS card_study_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    card_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    study_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    was_correct BOOLEAN NOT NULL,
    response_time_seconds INT,
    FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Drop existing triggers if they exist
DROP TRIGGER IF EXISTS trg_users_before_insert;
DROP TRIGGER IF EXISTS trg_users_before_update;

-- Helper functions to DRY trigger logic
DROP FUNCTION IF EXISTS normalize_capitalized_name;
DELIMITER $$
CREATE FUNCTION normalize_capitalized_name(s VARCHAR(255))
RETURNS VARCHAR(255) DETERMINISTIC
BEGIN
    IF s IS NULL OR LENGTH(s) = 0 THEN
        RETURN s;
    END IF;
    RETURN CONCAT(UPPER(SUBSTRING(s, 1, 1)), LOWER(SUBSTRING(s, 2)));
END$$
DELIMITER ;

DROP FUNCTION IF EXISTS is_valid_email_format;
DELIMITER $$
CREATE FUNCTION is_valid_email_format(e VARCHAR(255))
RETURNS BOOLEAN DETERMINISTIC
BEGIN
    RETURN e REGEXP '^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$';
END$$
DELIMITER ;

-- Trigger to capitalize first_name and last_name, and validate email on INSERT
DELIMITER $$
CREATE TRIGGER trg_users_before_insert
BEFORE INSERT ON users
FOR EACH ROW
BEGIN
    -- Normalize names
    SET NEW.first_name = normalize_capitalized_name(NEW.first_name);
    SET NEW.last_name = normalize_capitalized_name(NEW.last_name);

    -- Validate email format
    IF NOT is_valid_email_format(NEW.email) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Invalid email format';
    END IF;
END$$
DELIMITER ;

-- Trigger to capitalize first_name and last_name, and validate email on UPDATE
DELIMITER $$
CREATE TRIGGER trg_users_before_update
BEFORE UPDATE ON users
FOR EACH ROW
BEGIN
    -- Normalize names
    SET NEW.first_name = normalize_capitalized_name(NEW.first_name);
    SET NEW.last_name = normalize_capitalized_name(NEW.last_name);

    -- Validate email format
    IF NOT is_valid_email_format(NEW.email) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Invalid email format';
    END IF;
END$$
DELIMITER ;

-- Ensure legacy tables have required columns
SET @col_exists := (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'cards' AND COLUMN_NAME = 'completed'
);
SET @sql := IF(@col_exists = 0, 'ALTER TABLE cards ADD COLUMN completed BOOLEAN DEFAULT FALSE', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- users(username)
SET @idx_exists := (
    SELECT COUNT(1) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND INDEX_NAME = 'idx_users_username'
);
SET @sql := IF(@idx_exists = 0, 'CREATE INDEX idx_users_username ON users(username)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- users(email)
SET @idx_exists := (
    SELECT COUNT(1) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND INDEX_NAME = 'idx_users_email'
);
SET @sql := IF(@idx_exists = 0, 'CREATE INDEX idx_users_email ON users(email)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- decks(user_id)
SET @idx_exists := (
    SELECT COUNT(1) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'decks' AND INDEX_NAME = 'idx_decks_user_id'
);
SET @sql := IF(@idx_exists = 0, 'CREATE INDEX idx_decks_user_id ON decks(user_id)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- cards(deck_id)
SET @idx_exists := (
    SELECT COUNT(1) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'cards' AND INDEX_NAME = 'idx_cards_deck_id'
);
SET @sql := IF(@idx_exists = 0, 'CREATE INDEX idx_cards_deck_id ON cards(deck_id)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- cards(completed)
SET @idx_exists := (
    SELECT COUNT(1) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'cards' AND INDEX_NAME = 'idx_cards_completed'
);
SET @sql := IF(@idx_exists = 0, 'CREATE INDEX idx_cards_completed ON cards(completed)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- study_sessions(user_id)
SET @idx_exists := (
    SELECT COUNT(1) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'study_sessions' AND INDEX_NAME = 'idx_study_sessions_user_id'
);
SET @sql := IF(@idx_exists = 0, 'CREATE INDEX idx_study_sessions_user_id ON study_sessions(user_id)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- study_sessions(deck_id)
SET @idx_exists := (
    SELECT COUNT(1) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'study_sessions' AND INDEX_NAME = 'idx_study_sessions_deck_id'
);
SET @sql := IF(@idx_exists = 0, 'CREATE INDEX idx_study_sessions_deck_id ON study_sessions(deck_id)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- card_study_history(card_id)
SET @idx_exists := (
    SELECT COUNT(1) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'card_study_history' AND INDEX_NAME = 'idx_card_study_history_card_id'
);
SET @sql := IF(@idx_exists = 0, 'CREATE INDEX idx_card_study_history_card_id ON card_study_history(card_id)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- card_study_history(user_id)
SET @idx_exists := (
    SELECT COUNT(1) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'card_study_history' AND INDEX_NAME = 'idx_card_study_history_user_id'
);
SET @sql := IF(@idx_exists = 0, 'CREATE INDEX idx_card_study_history_user_id ON card_study_history(user_id)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Insert sample data for demonstration
INSERT IGNORE INTO users (username, email, password_hash, first_name, last_name) VALUES
('admin', 'admin@projectlume.com', 'demo', 'Admin', 'User'),
('testuser', 'test@projectlume.com', 'demo', 'Test', 'User');

-- Sample decks
INSERT IGNORE INTO decks (user_id, name, description) VALUES
(1, 'Java Basics', 'Basic Java programming concepts'),
(1, 'Data Structures', 'Common data structures and algorithms'),
(2, 'Web Development', 'HTML, CSS, and JavaScript fundamentals');

-- Sample cards
INSERT IGNORE INTO cards (deck_id, front_text, back_text, difficulty_level) VALUES
(1, 'What is the main method signature in Java?', 'public static void main(String[] args)', 'EASY'),
(1, 'What is inheritance in Java?', 'A mechanism where one class acquires the properties and behaviors of another class', 'MEDIUM'),
(1, 'What is polymorphism in Java?', 'The ability of an object to take on many forms, typically through method overriding and overloading', 'HARD'),
(2, 'What is the time complexity of binary search?', 'O(log n)', 'MEDIUM'),
(2, 'What is a hash table?', 'A data structure that implements an associative array abstract data type using a hash function', 'MEDIUM'),
(3, 'What does HTML stand for?', 'HyperText Markup Language', 'EASY'),
(3, 'What is CSS?', 'Cascading Style Sheets - used to style HTML elements', 'EASY');