package com.projectlume.integration;

import com.projectlume.model.User;
import com.projectlume.service.AuthService;
import com.projectlume.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration test for authentication flow
 */
public class AuthenticationFlowTest extends BaseTest {
    private AuthService authService;
    
    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        authService = new AuthService();
    }
    
    @Test
    public void testCompleteRegistrationFlow() throws Exception {
        String username = generateUniqueUsername();
        String email = generateUniqueEmail();
        String password = "TestPassword123!";
        String firstName = "Test";
        String lastName = "User";
        
        User registeredUser = authService.register(username, email, password, firstName, lastName);
        
        assertNotNull(registeredUser);
        assertNotNull(registeredUser.getId());
        assertEquals(username, registeredUser.getUsername());
        assertEquals(email, registeredUser.getEmail());
    }
    
    @Test
    public void testRegistrationThenLoginFlow() throws Exception {
        String username = generateUniqueUsername();
        String email = generateUniqueEmail();
        String password = "TestPassword123!";
        
        User registeredUser = authService.register(username, email, password, "Test", "User");
        
        User loggedInUser = authService.login(username, password);
        
        assertNotNull(loggedInUser);
        assertEquals(registeredUser.getId(), loggedInUser.getId());
        assertEquals(username, loggedInUser.getUsername());
    }
    
    @Test
    public void testRegistrationWithDuplicateUsername() throws Exception {
        String username = generateUniqueUsername();
        String email1 = generateUniqueEmail();
        String email2 = generateUniqueEmail();
        String password = "TestPassword123!";
        
        authService.register(username, email1, password, "First", "User");
        
        assertThrows(AuthService.AuthException.class, () -> {
            authService.register(username, email2, password, "Second", "User");
        });
    }
    
    @Test
    public void testLoginWithInvalidCredentials() throws Exception {
        String username = generateUniqueUsername();
        String email = generateUniqueEmail();
        String password = "TestPassword123!";
        
        authService.register(username, email, password, "Test", "User");
        
        assertThrows(AuthService.AuthException.class, () -> {
            authService.login(username, "WrongPassword123!");
        });
    }
    
    @Test
    public void testLoginWithNonExistentUser() throws Exception {
        assertThrows(AuthService.AuthException.class, () -> {
            authService.login("nonexistent", "password");
        });
    }
    
    @Test
    public void testRegistrationWithInvalidEmail() throws Exception {
        assertThrows(AuthService.AuthException.class, () -> {
            authService.register(generateUniqueUsername(), "invalid-email", "TestPassword123!", "Test", "User");
        });
    }
    
    @Test
    public void testRegistrationWithWeakPassword() throws Exception {
        assertThrows(AuthService.AuthException.class, () -> {
            authService.register(generateUniqueUsername(), generateUniqueEmail(), "weak", "Test", "User");
        });
    }
}

