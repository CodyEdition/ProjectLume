package com.projectlume.integration;

import com.projectlume.model.User;
import com.projectlume.service.AuthService;
import com.projectlume.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for profile update functionality
 * Tests username, email, password changes with validation and authorization
 */
public class ProfileUpdateTest extends BaseTest {
    private AuthService authService;
    private User testUser;
    
    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        authService = new AuthService();
        testUser = authService.register(generateUniqueUsername(), generateUniqueEmail(), 
                                       "TestPassword123!", "Original", "Name");
    }
    
    @Test
    public void testUpdateProfileWithValidData() throws Exception {
        // Act
        User updatedUser = authService.updateProfile(testUser.getId(), 
                                                    generateUniqueUsername(), 
                                                    generateUniqueEmail(), 
                                                    "Updated", "Name");
        
        // Assert
        assertNotNull(updatedUser);
        assertEquals("Updated", updatedUser.getFirstName());
        assertEquals("Name", updatedUser.getLastName());
    }
    
    @Test
    public void testUpdateProfileWithDuplicateUsername() throws Exception {
        // Arrange
        User otherUser = authService.register(generateUniqueUsername(), generateUniqueEmail(), 
                                            "TestPassword123!", "Other", "User");
        String otherUsername = otherUser.getUsername();
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            authService.updateProfile(testUser.getId(), otherUsername, testUser.getEmail(), 
                                    "Test", "User");
        });
    }
    
    @Test
    public void testChangePassword() throws Exception {
        // Arrange
        String currentPassword = "TestPassword123!";
        String newPassword = "NewPassword123!";
        
        // Act
        boolean changed = authService.changePassword(testUser.getId(), currentPassword, newPassword);
        
        // Assert
        assertTrue(changed);
        
        // Verify new password works
        User loggedInUser = authService.login(testUser.getUsername(), newPassword);
        assertNotNull(loggedInUser);
    }
    
    @Test
    public void testChangePasswordWithIncorrectCurrentPassword() {
        // Act & Assert
        assertThrows(AuthService.AuthException.class, () -> {
            authService.changePassword(testUser.getId(), "WrongPassword123!", "NewPassword123!");
        });
    }
    
    @Test
    public void testChangePasswordWithWeakNewPassword() {
        // Act & Assert
        assertThrows(AuthService.AuthException.class, () -> {
            authService.changePassword(testUser.getId(), "TestPassword123!", "weak");
        });
    }
}

