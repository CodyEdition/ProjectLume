package com.projectlume.integration;

import com.projectlume.model.Card;
import com.projectlume.model.Deck;
import com.projectlume.model.User;
import com.projectlume.service.AuthService;
import com.projectlume.service.CardService;
import com.projectlume.service.DeckService;
import com.projectlume.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration test for card CRUD operations
 * Tests card creation → read → update → delete with deck ownership validation
 */
public class CardCRUDFlowTest extends BaseTest {
    private CardService cardService;
    private DeckService deckService;
    private AuthService authService;
    private User testUser;
    private User otherUser;
    private Deck testDeck;
    private Deck otherDeck;
    
    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        cardService = new CardService();
        deckService = new DeckService();
        authService = new AuthService();
        
        testUser = authService.register(generateUniqueUsername(), generateUniqueEmail(), 
                                       "TestPassword123!", "Test", "User");
        otherUser = authService.register(generateUniqueUsername(), generateUniqueEmail(), 
                                        "TestPassword123!", "Other", "User");
        
        testDeck = deckService.createDeck(testUser.getId(), "Test Deck", "Description");
        otherDeck = deckService.createDeck(otherUser.getId(), "Other Deck", "Description");
    }
    
    @Test
    public void testCreateCardFlow() throws SQLException {
        // Act
        Card card = cardService.createCard(testUser.getId(), testDeck.getId(), 
                                          "Front Text", "Back Text", Card.DifficultyLevel.MEDIUM);
        
        // Assert
        assertNotNull(card);
        assertNotNull(card.getId());
        assertEquals("Front Text", card.getFrontText());
        assertEquals("Back Text", card.getBackText());
        assertEquals(testDeck.getId(), card.getDeckId());
    }
    
    @Test
    public void testCreateReadUpdateDeleteFlow() throws SQLException {
        // Arrange - Create card
        Card card = cardService.createCard(testUser.getId(), testDeck.getId(), 
                                          "Original Front", "Original Back", Card.DifficultyLevel.EASY);
        
        // Act - Read card
        Card retrievedCard = cardService.getCardForUser(testUser.getId(), card.getId());
        
        // Assert - Read successful
        assertNotNull(retrievedCard);
        assertEquals("Original Front", retrievedCard.getFrontText());
        
        // Act - Update card
        Card updatedCard = cardService.updateCard(testUser.getId(), card.getId(), 
                                                 "Updated Front", "Updated Back", Card.DifficultyLevel.HARD);
        
        // Assert - Update successful
        assertEquals("Updated Front", updatedCard.getFrontText());
        assertEquals("Updated Back", updatedCard.getBackText());
        assertEquals(Card.DifficultyLevel.HARD, updatedCard.getDifficultyLevel());
        
        // Act - Delete card
        boolean deleted = cardService.deleteCard(testUser.getId(), card.getId());
        
        // Assert - Delete successful
        assertTrue(deleted);
    }
    
    @Test
    public void testCardOwnershipThroughDeck() throws SQLException {
        // Arrange
        Card card = cardService.createCard(testUser.getId(), testDeck.getId(), 
                                          "Front", "Back", Card.DifficultyLevel.EASY);
        
        // Act & Assert - Other user cannot access card
        assertThrows(SecurityException.class, () -> {
            cardService.getCardForUser(otherUser.getId(), card.getId());
        });
        
        assertThrows(SecurityException.class, () -> {
            cardService.updateCard(otherUser.getId(), card.getId(), "Hacked", "Back", null);
        });
        
        assertThrows(SecurityException.class, () -> {
            cardService.deleteCard(otherUser.getId(), card.getId());
        });
    }
    
    @Test
    public void testCreateCardInNonOwnedDeck() {
        // Act & Assert
        assertThrows(SecurityException.class, () -> {
            cardService.createCard(testUser.getId(), otherDeck.getId(), "Front", "Back", null);
        });
    }
    
    @Test
    public void testListCardsForDeck() throws SQLException {
        // Arrange
        cardService.createCard(testUser.getId(), testDeck.getId(), "Front 1", "Back 1", null);
        cardService.createCard(testUser.getId(), testDeck.getId(), "Front 2", "Back 2", null);
        
        // Act
        List<Card> cards = cardService.getCardsForDeck(testUser.getId(), testDeck.getId());
        
        // Assert
        assertEquals(2, cards.size());
    }
    
    @Test
    public void testCreateCardWithInvalidData() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            cardService.createCard(testUser.getId(), testDeck.getId(), "", "Back", null);
        });
    }
}

