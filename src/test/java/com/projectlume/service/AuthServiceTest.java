package com.projectlume.service;

import com.projectlume.model.User;
import com.projectlume.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for AuthService
 * Covers user registration, login, password hashing, and error scenarios
 */
public class AuthServiceTest extends BaseTest {
    private AuthService authService;
    
    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        authService = new AuthService();
    }
    
    @Test
    public void testRegisterUserWithValidData() throws Exception {
        // Arrange
        String username = generateUniqueUsername();
        String email = generateUniqueEmail();
        String password = "TestPassword123!";
        String firstName = "Test";
        String lastName = "User";
        
        // Act
        User registeredUser = authService.register(username, email, password, firstName, lastName);
        
        // Assert
        assertNotNull(registeredUser);
        assertNotNull(registeredUser.getId());
        assertEquals(username, registeredUser.getUsername());
        assertEquals(email, registeredUser.getEmail());
        assertEquals(firstName, registeredUser.getFirstName());
        assertEquals(lastName, registeredUser.getLastName());
        assertTrue(registeredUser.isActive());
        assertNotNull(registeredUser.getPasswordHash());
        assertNotEquals(password, registeredUser.getPasswordHash()); // Password should be hashed
    }
    
    @Test
    public void testRegisterUserWithDuplicateUsername() throws Exception {
        // Arrange
        String username = generateUniqueUsername();
        String email1 = generateUniqueEmail();
        String email2 = generateUniqueEmail();
        String password = "TestPassword123!";
        
        // Create first user
        authService.register(username, email1, password, "First", "User");
        
        // Act & Assert
        AuthService.AuthException exception = assertThrows(AuthService.AuthException.class, () -> {
            authService.register(username, email2, password, "Second", "User");
        });
        
        assertTrue(exception.getMessage().contains("Username already exists"));
    }
    
    @Test
    public void testRegisterUserWithDuplicateEmail() throws Exception {
        // Arrange
        String username1 = generateUniqueUsername();
        String username2 = generateUniqueUsername();
        String email = generateUniqueEmail();
        String password = "TestPassword123!";
        
        // Create first user
        authService.register(username1, email, password, "First", "User");
        
        // Act & Assert
        AuthService.AuthException exception = assertThrows(AuthService.AuthException.class, () -> {
            authService.register(username2, email, password, "Second", "User");
        });
        
        assertTrue(exception.getMessage().contains("Email already exists"));
    }
    
    @Test
    public void testRegisterUserWithEmptyUsername() {
        // Act & Assert
        assertThrows(AuthService.AuthException.class, () -> {
            authService.register("", generateUniqueEmail(), "TestPassword123!", "Test", "User");
        });
    }
    
    @Test
    public void testRegisterUserWithInvalidEmail() {
        // Act & Assert
        assertThrows(AuthService.AuthException.class, () -> {
            authService.register(generateUniqueUsername(), "invalid-email", "TestPassword123!", "Test", "User");
        });
    }
    
    @Test
    public void testRegisterUserWithWeakPassword() {
        // Act & Assert - password validation should fail
        assertThrows(AuthService.AuthException.class, () -> {
            authService.register(generateUniqueUsername(), generateUniqueEmail(), "weak", "Test", "User");
        });
    }
    
    @Test
    public void testLoginWithValidCredentials() throws Exception {
        // Arrange
        String username = generateUniqueUsername();
        String email = generateUniqueEmail();
        String password = "TestPassword123!";
        
        User registeredUser = authService.register(username, email, password, "Test", "User");
        
        // Act
        User loggedInUser = authService.login(username, password);
        
        // Assert
        assertNotNull(loggedInUser);
        assertEquals(registeredUser.getId(), loggedInUser.getId());
        assertEquals(username, loggedInUser.getUsername());
        assertEquals(email, loggedInUser.getEmail());
    }
    
    @Test
    public void testLoginWithInvalidPassword() throws Exception {
        // Arrange
        String username = generateUniqueUsername();
        String email = generateUniqueEmail();
        String password = "TestPassword123!";
        
        authService.register(username, email, password, "Test", "User");
        
        // Act & Assert
        AuthService.AuthException exception = assertThrows(AuthService.AuthException.class, () -> {
            authService.login(username, "WrongPassword123!");
        });
        
        assertTrue(exception.getMessage().contains("Invalid") || 
                  exception.getMessage().contains("password") ||
                  exception.getMessage().contains("credentials"));
    }
    
    @Test
    public void testLoginWithNonExistentUser() {
        // Act & Assert
        AuthService.AuthException exception = assertThrows(AuthService.AuthException.class, () -> {
            authService.login("nonexistent", "password");
        });
        
        assertTrue(exception.getMessage().contains("not found") || 
                  exception.getMessage().contains("Invalid"));
    }
    
    @Test
    public void testPasswordHashing() throws Exception {
        // Arrange
        String username = generateUniqueUsername();
        String email = generateUniqueEmail();
        String password = "TestPassword123!";
        
        // Act
        User registeredUser = authService.register(username, email, password, "Test", "User");
        
        // Assert - password should be hashed using BCrypt
        assertTrue(BCrypt.checkpw(password, registeredUser.getPasswordHash()));
        assertNotEquals(password, registeredUser.getPasswordHash());
    }
    
    @Test
    public void testUpdateProfileWithValidData() throws Exception {
        // Arrange
        String username = generateUniqueUsername();
        String email = generateUniqueEmail();
        User user = authService.register(username, email, "TestPassword123!", "Original", "Name");
        
        String newUsername = generateUniqueUsername();
        String newEmail = generateUniqueEmail();
        
        // Act
        User updatedUser = authService.updateProfile(user.getId(), newUsername, newEmail, "Updated", "Name");
        
        // Assert
        assertNotNull(updatedUser);
        assertEquals(newUsername, updatedUser.getUsername());
        assertEquals(newEmail, updatedUser.getEmail());
        assertEquals("Updated", updatedUser.getFirstName());
        assertEquals("Name", updatedUser.getLastName());
    }
    
    @Test
    public void testUpdateProfileWithDuplicateUsername() throws Exception {
        // Arrange
        String username1 = generateUniqueUsername();
        String username2 = generateUniqueUsername();
        String email1 = generateUniqueEmail();
        String email2 = generateUniqueEmail();
        
        authService.register(username1, email1, "TestPassword123!", "User", "One");
        User user2 = authService.register(username2, email2, "TestPassword123!", "User", "Two");
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            authService.updateProfile(user2.getId(), username1, email2, "User", "Two");
        });
    }
}

