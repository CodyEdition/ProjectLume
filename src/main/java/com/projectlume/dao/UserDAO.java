package com.projectlume.dao;

import com.projectlume.model.User;
import com.projectlume.repository.UserRepository;
import com.projectlume.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Data Access Object for User entity
 * Handles all database operations related to users
 * Implements the Repository pattern for data access abstraction
 */
public class UserDAO implements UserRepository {
    private static final Logger logger = Logger.getLogger(UserDAO.class.getName());
    
    // SQL queries
    private static final String INSERT_USER = 
        "INSERT INTO users (username, email, password_hash, first_name, last_name, created_at, updated_at, is_active) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String SELECT_USER_BY_ID = 
        "SELECT * FROM users WHERE id = ? AND is_active = true";
    
    private static final String SELECT_USER_BY_USERNAME = 
        "SELECT * FROM users WHERE username = ? AND is_active = true";
    
    private static final String SELECT_USER_BY_EMAIL = 
        "SELECT * FROM users WHERE email = ? AND is_active = true";
    
    private static final String UPDATE_USER = 
        "UPDATE users SET username = ?, email = ?, first_name = ?, last_name = ?, updated_at = ? WHERE id = ?";
    
    private static final String DELETE_USER = 
        "UPDATE users SET is_active = false, updated_at = ? WHERE id = ?";
    
    private static final String SELECT_ALL_USERS = 
        "SELECT * FROM users WHERE is_active = true ORDER BY created_at DESC";
    
    /**
     * Create a new user
     * @param user User object to create
     * @return Created user with generated ID
     * @throws SQLException if database error occurs
     */
    public User create(User user) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_USER, Statement.RETURN_GENERATED_KEYS)) {
            
            LocalDateTime now = LocalDateTime.now();
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPasswordHash());
            statement.setString(4, capitalizeName(user.getFirstName()));
            statement.setString(5, capitalizeName(user.getLastName()));
            statement.setTimestamp(6, Timestamp.valueOf(now));
            statement.setTimestamp(7, Timestamp.valueOf(now));
            statement.setBoolean(8, user.isActive());
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getLong(1));
                    user.setCreatedAt(now);
                    user.setUpdatedAt(now);
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
            
            logger.info("User created successfully: " + user.getUsername());
            return user;
        }
    }
    
    /**
     * Find user by ID
     * @param id User ID
     * @return User object or null if not found
     * @throws SQLException if database error occurs
     */
    public User findById(Long id) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_USER_BY_ID)) {
            
            statement.setLong(1, id);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToUser(resultSet);
                }
            }
            
            return null;
        }
    }
    
    /**
     * Find user by username
     * @param username Username to search for
     * @return User object or null if not found
     * @throws SQLException if database error occurs
     */
    public User findByUsername(String username) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_USER_BY_USERNAME)) {
            
            statement.setString(1, username);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToUser(resultSet);
                }
            }
            
            return null;
        }
    }
    
    /**
     * Find user by email
     * @param email Email to search for
     * @return User object or null if not found
     * @throws SQLException if database error occurs
     */
    public User findByEmail(String email) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_USER_BY_EMAIL)) {
            
            statement.setString(1, email);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToUser(resultSet);
                }
            }
            
            return null;
        }
    }
    
    /**
     * Update user information
     * @param user User object with updated information
     * @return Updated user object
     * @throws SQLException if database error occurs
     */
    public User update(User user) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_USER)) {
            
            LocalDateTime now = LocalDateTime.now();
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getEmail());
            statement.setString(3, capitalizeName(user.getFirstName()));
            statement.setString(4, capitalizeName(user.getLastName()));
            statement.setTimestamp(5, Timestamp.valueOf(now));
            statement.setLong(6, user.getId());
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating user failed, no rows affected.");
            }
            
            user.setUpdatedAt(now);
            logger.info("User updated successfully: " + user.getUsername());
            return user;
        }
    }
    
    /**
     * Soft delete user (mark as inactive)
     * @param id User ID to delete
     * @return true if deletion successful
     * @throws SQLException if database error occurs
     */
    public boolean delete(Long id) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_USER)) {
            
            statement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            statement.setLong(2, id);
            
            int affectedRows = statement.executeUpdate();
            logger.info("User deleted successfully: ID " + id);
            return affectedRows > 0;
        }
    }
    
    /**
     * Get all active users
     * @return List of all active users
     * @throws SQLException if database error occurs
     */
    public List<User> findAll() throws SQLException {
        List<User> users = new ArrayList<>();
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL_USERS);
             ResultSet resultSet = statement.executeQuery()) {
            
            while (resultSet.next()) {
                users.add(mapResultSetToUser(resultSet));
            }
        }
        
        return users;
    }
    
    /**
     * Capitalize the first letter of a name (first letter uppercase, rest lowercase)
     * @param name Name to capitalize
     * @return Capitalized name, or null/empty string if input is null/empty
     */
    private String capitalizeName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return name;
        }
        name = name.trim();
        if (name.length() == 1) {
            return name.toUpperCase();
        }
        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
    }
    
    /**
     * Map ResultSet to User object
     * @param resultSet Database result set
     * @return User object
     * @throws SQLException if mapping fails
     */
    private User mapResultSetToUser(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getLong("id"));
        user.setUsername(resultSet.getString("username"));
        user.setEmail(resultSet.getString("email"));
        user.setPasswordHash(resultSet.getString("password_hash"));
        user.setFirstName(resultSet.getString("first_name"));
        user.setLastName(resultSet.getString("last_name"));
        
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = resultSet.getTimestamp("updated_at");
        if (updatedAt != null) {
            user.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        user.setActive(resultSet.getBoolean("is_active"));
        
        return user;
    }
}
