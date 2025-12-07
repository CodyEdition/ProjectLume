package com.projectlume.repository;

import com.projectlume.model.User;
import java.sql.SQLException;
import java.util.List;

/**
 * Repository interface for User entity
 * Defines the contract for user data access operations
 */
public interface UserRepository {
    
    /**
     * Creates a new user
     * @param user User object to create
     * @return Created user with generated ID
     * @throws SQLException if database error occurs
     */
    User create(User user) throws SQLException;
    
    /**
     * Finds user by ID
     * @param id User ID
     * @return User object or null if not found
     * @throws SQLException if database error occurs
     */
    User findById(Long id) throws SQLException;
    
    /**
     * Finds user by username
     * @param username Username to search for
     * @return User object or null if not found
     * @throws SQLException if database error occurs
     */
    User findByUsername(String username) throws SQLException;
    
    /**
     * Finds user by email
     * @param email Email to search for
     * @return User object or null if not found
     * @throws SQLException if database error occurs
     */
    User findByEmail(String email) throws SQLException;
    
    /**
     * Updates user information
     * @param user User object with updated information
     * @return Updated user object
     * @throws SQLException if database error occurs
     */
    User update(User user) throws SQLException;
    
    /**
     * Soft deletes user (marks as inactive)
     * @param id User ID to delete
     * @return true if deletion successful
     * @throws SQLException if database error occurs
     */
    boolean delete(Long id) throws SQLException;
    
    /**
     * Gets all active users
     * @return List of all active users
     * @throws SQLException if database error occurs
     */
    List<User> findAll() throws SQLException;
}
