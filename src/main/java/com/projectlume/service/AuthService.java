package com.projectlume.service;

import com.projectlume.dao.UserDAO;
import com.projectlume.exception.ValidationException;
import com.projectlume.factory.DAOFactory;
import com.projectlume.model.User;
import com.projectlume.observer.EventPublisher;
import com.projectlume.observer.impl.UserRegisteredEvent;
import com.projectlume.strategy.ValidationContext;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

public class AuthService extends BaseService {
    private final UserDAO userDAO;
    private final ValidationContext validationContext;
    
    public AuthService() {
        super();
        this.userDAO = DAOFactory.createUserDAO();
        this.validationContext = new ValidationContext();
    }
    
    public User register(String username, String email, String password, String firstName, String lastName) 
            throws AuthException {
        try {
            validationContext.validate("username", username, "username");
            validationContext.validate("email", email, "email");
            validationContext.validate("password", password, "password");
            validationContext.validate("firstName", firstName, "firstName");
            validationContext.validate("lastName", lastName, "lastName");
            
            if (userDAO.findByUsername(username) != null) {
                throw new AuthException("Username already exists");
            }
            
            if (userDAO.findByEmail(email) != null) {
                throw new AuthException("Email already exists");
            }
            
            String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
            
            User user = new User(username, email, passwordHash, firstName, lastName);
            User createdUser = executeCreate(() -> userDAO.create(user));
            
            EventPublisher.getInstance().publish(new UserRegisteredEvent(createdUser));
            
            logger.info("User registered successfully: " + username);
            return createdUser;
            
        } catch (SQLException e) {
            logger.severe("Database error during registration: " + e.getMessage());
            throw new AuthException("Registration failed due to database error", e);
        } catch (ValidationException e) {
            throw new AuthException(e.getMessage());
        }
    }
    
    public User login(String username, String password) throws AuthException {
        try {
            if (username == null || username.trim().isEmpty()) {
                throw new AuthException("Username is required");
            }
            if (password == null || password.trim().isEmpty()) {
                throw new AuthException("Password is required");
            }
            
            User user = userDAO.findByUsername(username);
            if (user == null) {
                user = userDAO.findByEmail(username);
            }
            
            if (user == null) {
                throw new AuthException("Invalid username or password");
            }
            
            String stored = user.getPasswordHash();
            boolean isBcrypt = stored != null && (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$"));
            boolean valid = false;
            if (isBcrypt) {
                valid = BCrypt.checkpw(password, stored);
            } else {
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
    
    public User updateProfile(Long userId, String username, String email, 
                             String firstName, String lastName) 
            throws ValidationException, SQLException {
        validationContext.validate("username", username, "username");
        validationContext.validate("email", email, "email");
        validationContext.validate("firstName", firstName, "firstName");
        validationContext.validate("lastName", lastName, "lastName");
        
        User user = userDAO.findById(userId);
        if (user == null) {
            throw new SQLException("User not found");
        }
        
        if (!username.equals(user.getUsername())) {
            User existingUser = userDAO.findByUsername(username);
            if (existingUser != null) {
                throw new ValidationException("username", "Username is already taken");
            }
        }
        
        if (!email.equals(user.getEmail())) {
            User existingUser = userDAO.findByEmail(email);
            if (existingUser != null) {
                throw new ValidationException("email", "Email is already taken");
            }
        }
        
        user.setUsername(username);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        
        User updatedUser = executeUpdate(() -> userDAO.update(user));
        logger.info("Profile updated successfully for user: " + username);
        return updatedUser;
    }
    
    public boolean changePassword(Long userId, String currentPassword, String newPassword) throws AuthException {
        try {
            if (currentPassword == null || currentPassword.trim().isEmpty()) {
                throw new AuthException("Current password is required");
            }
            if (newPassword == null || newPassword.trim().isEmpty()) {
                throw new AuthException("New password is required");
            }
            if (newPassword.length() < 6) {
                throw new AuthException("New password must be at least 6 characters long");
            }
            
            User user = userDAO.findById(userId);
            if (user == null) {
                throw new AuthException("User not found");
            }
            
            String stored = user.getPasswordHash();
            boolean isBcrypt = stored != null && (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$"));
            boolean valid = false;
            if (isBcrypt) {
                valid = BCrypt.checkpw(currentPassword, stored);
            } else {
                valid = stored != null && stored.equals(currentPassword);
            }
            if (!valid) {
                throw new AuthException("Current password is incorrect");
            }
            
            String newPasswordHash = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            
            userDAO.updatePassword(userId, newPasswordHash);
            
            logger.info("Password changed successfully for user: " + user.getUsername());
            return true;
            
        } catch (SQLException e) {
            logger.severe("Database error during password change: " + e.getMessage());
            throw new AuthException("Password change failed due to database error", e);
        }
    }
    
    public static class AuthException extends Exception {
        public AuthException(String message) {
            super(message);
        }
        
        public AuthException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
