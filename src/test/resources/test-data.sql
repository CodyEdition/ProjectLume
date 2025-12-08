-- Test Data Setup Script
-- This file contains sample test data for integration tests
-- Note: Tests use BaseTest which handles setup/teardown automatically
-- This file is provided for reference or manual test data setup

-- Sample Users (for reference only - tests create unique users)
-- INSERT INTO users (username, email, password_hash, first_name, last_name, is_active) 
-- VALUES ('testuser1', 'test1@test.com', '$2a$10$...', 'Test', 'User', TRUE);

-- Sample Decks (for reference only)
-- INSERT INTO decks (user_id, name, description, is_active) 
-- VALUES (1, 'Test Deck', 'Test Description', TRUE);

-- Sample Cards (for reference only)
-- INSERT INTO cards (deck_id, front_text, back_text, difficulty_level, is_active) 
-- VALUES (1, 'Front Text', 'Back Text', 'MEDIUM', TRUE);

-- Sample Study Sessions (for reference only)
-- INSERT INTO study_sessions (user_id, deck_id, cards_studied, correct_answers, incorrect_answers, session_duration_minutes) 
-- VALUES (1, 1, 5, 4, 1, 10);

