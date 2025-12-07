package com.projectlume.service;

import com.projectlume.dao.UserDAO;
import com.projectlume.exception.ValidationException;
import com.projectlume.factory.DAOFactory;
import com.projectlume.model.User;
import com.projectlume.util.ValidationUtils;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Authentication service for user login, registration, and password management
 */
public class AuthService {
    private static final Logger logger = Logger.getLogger(AuthService.class.getName());
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    private final UserDAO userDAO;
    
    public AuthService() {
        this.userDAO = DAOFactory.createUserDAO();
    }
    
    /**
     * Register a new user
     * @param username Username
     * @param email Email address
     * @param password Plain text password
     * @param firstName First name
     * @param lastName Last name
     * @return Created user object
     * @throws AuthException if registration fails
     */
    public User register(String username, String email, String password, String firstName, String lastName) 
            throws AuthException {
        try {
            // Validate input
            validateRegistrationInput(username, email, password, firstName, lastName);
            
            // Check if username already exists
            if (userDAO.findByUsername(username) != null) {
                throw new AuthException("Username already exists");
            }
            
            // Check if email already exists
            if (userDAO.findByEmail(email) != null) {
                throw new AuthException("Email already exists");
            }
            
            // Hash password
            String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
            
            // Create user
            User user = new User(username, email, passwordHash, firstName, lastName);
            User createdUser = userDAO.create(user);
            
            logger.info("User registered successfully: " + username);
            return createdUser;
            
        } catch (SQLException e) {
            logger.severe("Database error during registration: " + e.getMessage());
            throw new AuthException("Registration failed due to database error", e);
        }
    }
    
    /**
     * Authenticate user login
     * @param username Username or email
     * @param password Plain text password
     * @return User object if authentication successful
     * @throws AuthException if authentication fails
     */
    public User login(String username, String password) throws AuthException {
        try {
            // Validate input
            if (username == null || username.trim().isEmpty()) {
                throw new AuthException("Username is required");
            }
            if (password == null || password.trim().isEmpty()) {
                throw new AuthException("Password is required");
            }
            
            // Find user by username or email
            User user = userDAO.findByUsername(username);
            if (user == null) {
                user = userDAO.findByEmail(username);
            }
            
            if (user == null) {
                throw new AuthException("Invalid username or password");
            }
            
            // Verify password (supports bcrypt or plaintext fallback for demo-only data)
            String stored = user.getPasswordHash();
            boolean isBcrypt = stored != null && (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$"));
            boolean valid = false;
            if (isBcrypt) {
                valid = BCrypt.checkpw(password, stored);
            } else {
                // Plaintext fallback for demo data
                valid = stored != null && stored.equals(password);
            }
            if (!valid) {
                throw new AuthException("Invalid username or password");
            }
            
            logger.info("User logged in successfully: " + user.getUsername());
            return user;
            
        } catch (SQLException e) {
            logger.severe("Database error during login: " + e.getMessage());
            throw new AuthException("Login failed due to database error", e);
        }
    }
    
    /**
     * Update user profile information
     * @param userId User ID
     * @param username New username
     * @param email New email
     * @param firstName New first name
     * @param lastName New last name
     * @return Updated User object
     * @throws ValidationException if validation fails
     * @throws SQLException if database error occurs
     */
    public User updateProfile(Long userId, String username, String email, 
                             String firstName, String lastName) 
            throws ValidationException, SQLException {
        // Validate input
        ValidationUtils.validateUsername(username);
        ValidationUtils.validateEmail(email);
        ValidationUtils.validateName(firstName, "firstName");
        ValidationUtils.validateName(lastName, "lastName");
        
        // Get current user
        User user = userDAO.findById(userId);
        if (user == null) {
            throw new SQLException("User not found");
        }
        
        // Check username uniqueness (excluding current user)
        if (!username.equals(user.getUsername())) {
            User existingUser = userDAO.findByUsername(username);
            if (existingUser != null) {
                throw new ValidationException("username", "Username is already taken");
            }
        }
        
        // Check email uniqueness (excluding current user)
        if (!email.equals(user.getEmail())) {
            User existingUser = userDAO.findByEmail(email);
            if (existingUser != null) {
                throw new ValidationException("email", "Email is already taken");
            }
        }
        
        // Update user information
        user.setUsername(username);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        
        User updatedUser = userDAO.update(user);
        logger.info("Profile updated successfully for user: " + username);
        return updatedUser;
    }
    
    /**
     * Change user password
     * @param userId User ID
     * @param currentPassword Current password
     * @param newPassword New password
     * @return true if password changed successfully
     * @throws AuthException if password change fails
     */
    public boolean changePassword(Long userId, String currentPassword, String newPassword) throws AuthException {
        try {
            // Validate input
            if (currentPassword == null || currentPassword.trim().isEmpty()) {
                throw new AuthException("Current password is required");
            }
            if (newPassword == null || newPassword.trim().isEmpty()) {
                throw new AuthException("New password is required");
            }
            if (newPassword.length() < 6) {
                throw new AuthException("New password must be at least 6 characters long");
            }
            
            // Get user
            User user = userDAO.findById(userId);
            if (user == null) {
                throw new AuthException("User not found");
            }
            
            // Verify current password (supports bcrypt or plaintext fallback for demo-only data)
            String stored = user.getPasswordHash();
            boolean isBcrypt = stored != null && (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$"));
            boolean valid = false;
            if (isBcrypt) {
                valid = BCrypt.checkpw(currentPassword, stored);
            } else {
                // Plaintext fallback for demo data
                valid = stored != null && stored.equals(currentPassword);
            }
            if (!valid) {
                throw new AuthException("Current password is incorrect");
            }
            
            // Hash new password
            String newPasswordHash = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            
            // Update password in database
            userDAO.updatePassword(userId, newPasswordHash);
            
            logger.info("Password changed successfully for user: " + user.getUsername());
            return true;
            
        } catch (SQLException e) {
            logger.severe("Database error during password change: " + e.getMessage());
            throw new AuthException("Password change failed due to database error", e);
        }
    }
    
    /**
     * Validate registration input
     * @param username Username
     * @param email Email
     * @param password Password
     * @param firstName First name
     * @param lastName Last name
     * @throws AuthException if validation fails
     */
    private void validateRegistrationInput(String username, String email, String password, 
                                        String firstName, String lastName) throws AuthException {
        if (username == null || username.trim().isEmpty()) {
            throw new AuthException("Username is required");
        }
        if (username.length() < 3) {
            throw new AuthException("Username must be at least 3 characters long");
        }
        if (username.length() > 50) {
            throw new AuthException("Username must be less than 50 characters");
        }
        
        if (email == null || email.trim().isEmpty()) {
            throw new AuthException("Email is required");
        }
        if (!isValidEmail(email)) {
            throw new AuthException("Invalid email format");
        }
        
        if (password == null || password.trim().isEmpty()) {
            throw new AuthException("Password is required");
        }
        if (password.length() < 6) {
            throw new AuthException("Password must be at least 6 characters long");
        }
        
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new AuthException("First name is required");
        }
        
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new AuthException("Last name is required");
        }
    }
    
    /**
     * Simple email validation
     * @param email Email to validate
     * @return true if email format is valid
     */
    private boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }
    
    /**
     * Custom exception for authentication errors
     */
    public static class AuthException extends Exception {
        public AuthException(String message) {
            super(message);
        }
        
        public AuthException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
