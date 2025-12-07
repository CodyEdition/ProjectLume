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
 * DeckDAO
 * ----------------------------------------
 * Handles all CRUD operations and statistics queries
 * related to Deck entities. Provides SQL-based data access
 * for DeckService and other layers. Implements logic for:
 *
 *  - Creating decks
 *  - Updating deck data
 *  - Fetching decks for a user
 *  - Soft deletion
 *  - Statistics required by DeckStatsDTO
 *
 * Statistics include:
 *  - Total number of cards in a deck
 *  - How many unique cards a user has studied
 *  - The last study date for that deck
 *  - User's average accuracy (%) for that deck
 */
public class DeckDAO implements DeckRepository {

    private static final Logger logger = Logger.getLogger(DeckDAO.class.getName());

    // ======================
    // Base CRUD Queries
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
    // Statistics Queries (Used for DeckStatsDTO)
    // ======================

    /** Count active cards in a deck */
    private static final String COUNT_CARDS_IN_DECK =
            "SELECT COUNT(*) FROM cards WHERE deck_id = ? AND is_active = true";

    /** Count unique cards studied by the user */
    private static final String COUNT_STUDIED_CARDS_IN_DECK =
            "SELECT COUNT(DISTINCT c.id) " +
            "FROM cards c " +
            "JOIN card_study_history h ON c.id = h.card_id " +
            "WHERE c.deck_id = ? AND h.user_id = ? AND c.is_active = true";

    /** Get the most recent study date */
    private static final String LAST_STUDY_DATE_FOR_DECK =
            "SELECT MAX(h.study_date) " +
            "FROM cards c " +
            "JOIN card_study_history h ON c.id = h.card_id " +
            "WHERE c.deck_id = ? AND h.user_id = ? AND c.is_active = true";

    /** Compute average accuracy for this deck */
    private static final String ACCURACY_FOR_DECK =
            "SELECT AVG(CASE WHEN h.was_correct = true THEN 1.0 ELSE 0.0 END) " +
            "FROM cards c " +
            "JOIN card_study_history h ON c.id = h.card_id " +
            "WHERE c.deck_id = ? AND h.user_id = ? AND c.is_active = true";


    // =====================================================================================
    //                                         CRUD METHODS
    // =====================================================================================

    /**
     * Create a new deck and return the populated Deck object.
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

            // Retrieve generated ID
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
     * Retrieve a deck by its unique ID.
     */
    public Deck findById(Long id) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_DECK_BY_ID)) {
            
            statement.setLong(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDeck(rs);
                }
            }
        }
        return null;
    }

    /**
     * Retrieve all active decks belonging to a user.
     */
    public List<Deck> findByUserId(Long userId) throws SQLException {
        List<Deck> decks = new ArrayList<>();
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_DECKS_BY_USER_ID)) {
            
            statement.setLong(1, userId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    decks.add(mapResultSetToDeck(rs));
                }
            }
        }
        return decks;
    }

    /**
     * Update the name/description for a deck.
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
            return deck;
        }
    }

    /**
     * Soft delete a deck (mark as inactive).
     */
    public boolean delete(Long id) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_DECK)) {
            
            statement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            statement.setLong(2, id);
            
            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * Retrieve all active decks in the system.
     */
    public List<Deck> findAll() throws SQLException {
        List<Deck> decks = new ArrayList<>();
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL_DECKS);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                decks.add(mapResultSetToDeck(rs));
            }
        }
        return decks;
    }


    // =====================================================================================
    //                                 STATISTICS METHODS
    // =====================================================================================

    /**
     * Retrieves the total number of active cards in a deck.
     * Used for calculating completionPercentage.
     */
    public int getCardCountForDeck(Long deckId) throws SQLException {
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
     * Retrieves how many unique cards in this deck the user has studied.
     * Used to determine the user's progress.
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
            logger.warning("Error retrieving studied card count: deck=" + deckId + ", user=" + userId);
        }
        return 0;
    }

    /**
     * Retrieves the most recent study date for any card in the deck by this user.
     * Returns null if the deck has never been studied.
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
            logger.warning("Error retrieving last study date: deck=" + deckId + ", user=" + userId);
        }
        return null;
    }

    /**
     * Computes the average accuracy for the user in this deck.
     * Returns:
     *   - null if no study history exists
     *   - a Double representing accuracy percentage (0–100)
     */
    public Double getAccuracyForDeck(Long deckId, Long userId) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(ACCURACY_FOR_DECK)) {
            
            statement.setLong(1, deckId);
            statement.setLong(2, userId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    double accuracy = rs.getDouble(1);
                    if (rs.wasNull()) return null; // No study attempts
                    return accuracy * 100.0;       // Convert fraction to %
                }
            }

        } catch (SQLException e) {
            logger.warning("Error retrieving accuracy: deck=" + deckId + ", user=" + userId);
        }
        return null;
    }


    // =====================================================================================
    //                               INTERNAL MAPPING HELPER
    // =====================================================================================

    /**
     * Converts a SQL ResultSet row into a Deck object.
     */
    private Deck mapResultSetToDeck(ResultSet rs) throws SQLException {
        Deck deck = new Deck();

        deck.setId(rs.getLong("id"));
        deck.setUserId(rs.getLong("user_id"));
        deck.setName(rs.getString("name"));
        deck.setDescription(rs.getString("description"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null)
            deck.setCreatedAt(createdAt.toLocalDateTime());

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null)
            deck.setUpdatedAt(updatedAt.toLocalDateTime());

        deck.setActive(rs.getBoolean("is_active"));

        return deck;
    }
}
