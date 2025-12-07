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

    // ======================
    // Existing SQL Queries
    // ======================

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


    // ======================
    // NEW — Statistics Queries
    // ======================

    private static final String COUNT_CARDS_IN_DECK =
        "SELECT COUNT(*) FROM cards WHERE deck_id = ? AND is_active = true";

    private static final String COUNT_STUDIED_CARDS_IN_DECK =
        "SELECT COUNT(DISTINCT c.id) " +
        "FROM cards c " +
        "JOIN card_study_history h ON c.id = h.card_id " +
        "WHERE c.deck_id = ? AND h.user_id = ? AND c.is_active = true";

    private static final String LAST_STUDY_DATE_FOR_DECK =
        "SELECT MAX(h.study_date) " +
        "FROM cards c " +
        "JOIN card_study_history h ON c.id = h.card_id " +
        "WHERE c.deck_id = ? AND h.user_id = ? AND c.is_active = true";

    private static final String ACCURACY_FOR_DECK =
        "SELECT AVG(CASE WHEN h.was_correct = true THEN 1.0 ELSE 0 END) " +
        "FROM cards c " +
        "JOIN card_study_history h ON c.id = h.card_id " +
        "WHERE c.deck_id = ? AND h.user_id = ? AND c.is_active = true";


    // ======================
    // CRUD Operations
    // ======================

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


    // ======================
    // NEW — Statistics Methods
    // ======================

    /**
     * Returns the total number of active cards in a deck.
     */
    public int getCardCountForDeck(Long deckId) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(COUNT_CARDS_IN_DECK)) {

            statement.setLong(1, deckId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }

        } catch (SQLException e) {
            logger.warning("Error retrieving card count for deck " + deckId + ": " + e.getMessage());
        }
        return 0;
    }

    /**
     * Returns the number of unique cards from a deck that the user has studied.
     */
    public int getStudiedCardCountForDeck(Long deckId, Long userId) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(COUNT_STUDIED_CARDS_IN_DECK)) {

            statement.setLong(1, deckId);
            statement.setLong(2, userId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }

        } catch (SQLException e) {
            logger.warning("Error retrieving studied card count for deck "
                    + deckId + ", user " + userId + ": " + e.getMessage());
        }
        return 0;
    }

    /**
     * Returns the most recent study date for any card in the deck by the given user.
     */
    public LocalDateTime getLastStudyDateForDeck(Long deckId, Long userId) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(LAST_STUDY_DATE_FOR_DECK)) {

            statement.setLong(1, deckId);
            statement.setLong(2, userId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    Timestamp ts = rs.getTimestamp(1);
                    return ts != null ? ts.toLocalDateTime() : null;
                }
            }

        } catch (SQLException e) {
            logger.warning("Error retrieving last study date for deck "
                    + deckId + ", user " + userId + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * Returns the average accuracy (0–100%) for all study attempts on the deck by the user.
     */
    public double getAccuracyForDeck(Long deckId, Long userId) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(ACCURACY_FOR_DECK)) {

            statement.setLong(1, deckId);
            statement.setLong(2, userId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    double avg = rs.getDouble(1);
                    if (rs.wasNull()) return 0.0;
                    return avg * 100.0;
                }
            }

        } catch (SQLException e) {
            logger.warning("Error retrieving accuracy for deck "
                    + deckId + ", user " + userId + ": " + e.getMessage());
        }

        return 0.0;
    }


    // ======================
    // Mapping Helper
    // ======================

    private Deck mapResultSetToDeck(ResultSet resultSet) throws SQLException {
        Deck deck = new Deck();
        deck.setId(resultSet.getLong("id"));
        deck.setUserId(resultSet.getLong("user_id"));
        deck.setName(resultSet.getString("name"));
        deck.setDescription(resultSet.getString("description"));

        Timestamp createdAt = resultSet.getTimestamp("created_at");
        if (createdAt != null) deck.setCreatedAt(createdAt.toLocalDateTime());

        Timestamp updatedAt = resultSet.getTimestamp("updated_at");
        if (updatedAt != null) deck.setUpdatedAt(updatedAt.toLocalDateTime());

        deck.setActive(resultSet.getBoolean("is_active"));
        return deck;
    }
}
