package com.projectlume.integration;

import com.projectlume.model.Deck;
import com.projectlume.model.User;
import com.projectlume.service.AuthService;
import com.projectlume.service.DeckService;
import com.projectlume.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration test for deck management flow
 * Tests deck creation → update → delete flow with ownership validation
 */
public class DeckManagementFlowTest extends BaseTest {
    private DeckService deckService;
    private AuthService authService;
    private User testUser;
    private User otherUser;
    
    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        deckService = new DeckService();
        authService = new AuthService();
        
        testUser = authService.register(generateUniqueUsername(), generateUniqueEmail(), 
                                       "TestPassword123!", "Test", "User");
        otherUser = authService.register(generateUniqueUsername(), generateUniqueEmail(), 
                                        "TestPassword123!", "Other", "User");
    }
    
    @Test
    public void testCreateDeckFlow() throws SQLException {
        // Act
        Deck deck = deckService.createDeck(testUser.getId(), "My Deck", "Description");
        
        // Assert
        assertNotNull(deck);
        assertNotNull(deck.getId());
        assertEquals("My Deck", deck.getName());
        assertEquals(testUser.getId(), deck.getUserId());
    }
    
    @Test
    public void testCreateUpdateDeleteFlow() throws SQLException {
        // Arrange - Create deck
        Deck deck = deckService.createDeck(testUser.getId(), "Original Name", "Original Description");
        
        // Act - Update deck
        Deck updatedDeck = deckService.updateDeck(testUser.getId(), deck.getId(), 
                                                   "Updated Name", "Updated Description");
        
        // Assert - Update successful
        assertEquals("Updated Name", updatedDeck.getName());
        assertEquals("Updated Description", updatedDeck.getDescription());
        
        // Act - Delete deck
        boolean deleted = deckService.deleteDeck(testUser.getId(), deck.getId());
        
        // Assert - Delete successful
        assertTrue(deleted);
    }
    
    @Test
    public void testDeckOwnershipValidation() throws SQLException {
        // Arrange
        Deck deck = deckService.createDeck(testUser.getId(), "My Deck", "Description");
        
        // Act & Assert - Other user cannot access deck
        assertThrows(SecurityException.class, () -> {
            deckService.getDeckForUser(otherUser.getId(), deck.getId());
        });
        
        assertThrows(SecurityException.class, () -> {
            deckService.updateDeck(otherUser.getId(), deck.getId(), "Hacked", "Description");
        });
        
        assertThrows(SecurityException.class, () -> {
            deckService.deleteDeck(otherUser.getId(), deck.getId());
        });
    }
    
    @Test
    public void testListUserDecks() throws SQLException {
        // Arrange
        deckService.createDeck(testUser.getId(), "Deck 1", "Description 1");
        deckService.createDeck(testUser.getId(), "Deck 2", "Description 2");
        deckService.createDeck(otherUser.getId(), "Other Deck", "Description");
        
        // Act
        List<Deck> userDecks = deckService.getDecksForUser(testUser.getId());
        
        // Assert
        assertEquals(2, userDecks.size());
        assertTrue(userDecks.stream().allMatch(d -> d.getUserId().equals(testUser.getId())));
    }
    
    @Test
    public void testCreateDeckWithInvalidName() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            deckService.createDeck(testUser.getId(), "", "Description");
        });
    }
}

