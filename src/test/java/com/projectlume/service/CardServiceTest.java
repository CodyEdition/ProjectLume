package com.projectlume.service;

import com.projectlume.dao.CardDAO;
import com.projectlume.factory.DAOFactory;
import com.projectlume.model.Card;
import com.projectlume.model.Deck;
import com.projectlume.model.User;
import com.projectlume.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for CardService
 * Covers card CRUD operations, deck ownership validation, input validation, and error scenarios
 */
public class CardServiceTest extends BaseTest {
    private CardService cardService;
    private DeckService deckService;
    private AuthService authService;
    private CardDAO cardDAO;
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
        cardDAO = DAOFactory.createCardDAO();
        
        testUser = authService.register(generateUniqueUsername(), generateUniqueEmail(), 
                                       "TestPassword123!", "Test", "User");
        otherUser = authService.register(generateUniqueUsername(), generateUniqueEmail(), 
                                         "TestPassword123!", "Other", "User");
        
        testDeck = deckService.createDeck(testUser.getId(), "Test Deck", "Description");
        otherDeck = deckService.createDeck(otherUser.getId(), "Other Deck", "Description");
    }
    
    @Test
    public void testCreateCardWithValidData() throws SQLException {
        String frontText = "Front of card";
        String backText = "Back of card";
        Card.DifficultyLevel difficulty = Card.DifficultyLevel.MEDIUM;
        
        Card createdCard = cardService.createCard(testUser.getId(), testDeck.getId(), 
                                                 frontText, backText, difficulty);
        
        assertNotNull(createdCard);
        assertNotNull(createdCard.getId());
        assertEquals(frontText, createdCard.getFrontText());
        assertEquals(backText, createdCard.getBackText());
        assertEquals(difficulty, createdCard.getDifficultyLevel());
        assertEquals(testDeck.getId(), createdCard.getDeckId());
    }
    
    @Test
    public void testCreateCardWithEmptyFrontText() {
        assertThrows(IllegalArgumentException.class, () -> {
            cardService.createCard(testUser.getId(), testDeck.getId(), "", "Back", null);
        });
    }
    
    @Test
    public void testCreateCardWithEmptyBackText() {
        assertThrows(IllegalArgumentException.class, () -> {
            cardService.createCard(testUser.getId(), testDeck.getId(), "Front", "", null);
        });
    }
    
    @Test
    public void testCreateCardInNonOwnedDeck() {
        assertThrows(SecurityException.class, () -> {
            cardService.createCard(testUser.getId(), otherDeck.getId(), "Front", "Back", null);
        });
    }
    
    @Test
    public void testGetCardForUserOwnedCard() throws SQLException {
        Card card = cardService.createCard(testUser.getId(), testDeck.getId(), 
                                          "Front", "Back", Card.DifficultyLevel.EASY);
        
        Card retrievedCard = cardService.getCardForUser(testUser.getId(), card.getId());
        
        assertNotNull(retrievedCard);
        assertEquals(card.getId(), retrievedCard.getId());
        assertEquals(card.getFrontText(), retrievedCard.getFrontText());
    }
    
    @Test
    public void testGetCardForUserNonOwnedCard() throws SQLException {
        Card card = cardService.createCard(otherUser.getId(), otherDeck.getId(), 
                                          "Front", "Back", Card.DifficultyLevel.EASY);
        
        assertThrows(SecurityException.class, () -> {
            cardService.getCardForUser(testUser.getId(), card.getId());
        });
    }
    
    @Test
    public void testGetCardsForDeck() throws SQLException {
        cardService.createCard(testUser.getId(), testDeck.getId(), "Front 1", "Back 1", null);
        cardService.createCard(testUser.getId(), testDeck.getId(), "Front 2", "Back 2", null);
        
        List<Card> cards = cardService.getCardsForDeck(testUser.getId(), testDeck.getId());
        
        assertEquals(2, cards.size());
    }
    
    @Test
    public void testGetCardsForDeckNonOwned() {
        assertThrows(SecurityException.class, () -> {
            cardService.getCardsForDeck(testUser.getId(), otherDeck.getId());
        });
    }
    
    @Test
    public void testUpdateCardWithValidData() throws SQLException {
        Card card = cardService.createCard(testUser.getId(), testDeck.getId(), 
                                          "Original Front", "Original Back", Card.DifficultyLevel.EASY);
        String newFront = "Updated Front";
        String newBack = "Updated Back";
        
        Card updatedCard = cardService.updateCard(testUser.getId(), card.getId(), 
                                                  newFront, newBack, Card.DifficultyLevel.HARD);
        
        assertNotNull(updatedCard);
        assertEquals(newFront, updatedCard.getFrontText());
        assertEquals(newBack, updatedCard.getBackText());
        assertEquals(Card.DifficultyLevel.HARD, updatedCard.getDifficultyLevel());
    }
    
    @Test
    public void testUpdateCardWithInvalidData() throws SQLException {
        Card card = cardService.createCard(testUser.getId(), testDeck.getId(), 
                                          "Front", "Back", null);
        
        assertThrows(IllegalArgumentException.class, () -> {
            cardService.updateCard(testUser.getId(), card.getId(), "", "Back", null);
        });
    }
    
    @Test
    public void testUpdateCardNonOwned() throws SQLException {
        Card card = cardService.createCard(otherUser.getId(), otherDeck.getId(), 
                                          "Front", "Back", null);
        
        assertThrows(SecurityException.class, () -> {
            cardService.updateCard(testUser.getId(), card.getId(), "Hacked", "Back", null);
        });
    }
    
    @Test
    public void testDeleteCard() throws SQLException {
        Card card = cardService.createCard(testUser.getId(), testDeck.getId(), 
                                          "Front", "Back", null);
        
        boolean deleted = cardService.deleteCard(testUser.getId(), card.getId());
        
        assertTrue(deleted);
        Card deletedCard = cardDAO.findById(card.getId());
        assertNull(deletedCard);
    }
    
    @Test
    public void testDeleteCardNonOwned() throws SQLException {
        Card card = cardService.createCard(otherUser.getId(), otherDeck.getId(), 
                                          "Front", "Back", null);
        
        assertThrows(SecurityException.class, () -> {
            cardService.deleteCard(testUser.getId(), card.getId());
        });
    }
}

