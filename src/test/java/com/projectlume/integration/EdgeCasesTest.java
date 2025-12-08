package com.projectlume.integration;

import com.projectlume.model.User;
import com.projectlume.service.AuthService;
import com.projectlume.service.DeckService;
import com.projectlume.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for edge cases and error scenarios
 */
public class EdgeCasesTest extends BaseTest {
    private AuthService authService;
    private DeckService deckService;
    private User testUser;
    
    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        authService = new AuthService();
        deckService = new DeckService();
        
        testUser = authService.register(generateUniqueUsername(), generateUniqueEmail(), 
                                       "TestPassword123!", "Test", "User");
    }
    
    @Test
    public void testNullInputs() {
        assertThrows(Exception.class, () -> {
            authService.register(null, generateUniqueEmail(), "TestPassword123!", "Test", "User");
        });
        
        assertThrows(Exception.class, () -> {
            authService.register(generateUniqueUsername(), null, "TestPassword123!", "Test", "User");
        });
    }
    
    @Test
    public void testEmptyStringInputs() {
        assertThrows(Exception.class, () -> {
            authService.register("", generateUniqueEmail(), "TestPassword123!", "Test", "User");
        });
        
        assertThrows(Exception.class, () -> {
            authService.register(generateUniqueUsername(), generateUniqueEmail(), "", "Test", "User");
        });
    }
    
    @Test
    public void testSQLInjectionAttempt() throws Exception {
        String sqlInjection = "admin' OR '1'='1";
        try {
            authService.register(sqlInjection, generateUniqueEmail(), "TestPassword123!", "Test", "User");
        } catch (Exception e) {
        }
        
        com.projectlume.model.Deck deck = deckService.createDeck(testUser.getId(), "Normal Deck", "Description");
        try {
            deckService.updateDeck(testUser.getId(), deck.getId(), 
                                  "'; DROP TABLE users; --", "Description");
        } catch (Exception e) {
        }
    }
    
    @Test
    public void testXSSAttempt() throws Exception {
        String xssPayload = "<script>alert('XSS')</script>";
        try {
            deckService.createDeck(testUser.getId(), xssPayload, "Description");
        } catch (Exception e) {
        }
    }
    
    @Test
    public void testVeryLongInputs() {
        String longUsername = "a".repeat(1000);
        assertThrows(Exception.class, () -> {
            authService.register(longUsername, generateUniqueEmail(), "TestPassword123!", "Test", "User");
        });
    }
    
    @Test
    public void testSpecialCharacters() throws Exception {
        String specialChars = "Deck !@#$%^&*()";
        com.projectlume.model.Deck deck = deckService.createDeck(testUser.getId(), specialChars, "Description");
        assertNotNull(deck);
        assertEquals(specialChars, deck.getName());
    }
}

