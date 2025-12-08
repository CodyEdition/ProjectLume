package com.projectlume.dao;

import com.projectlume.factory.DAOFactory;
import com.projectlume.model.Card;
import com.projectlume.model.Deck;
import com.projectlume.model.User;
import com.projectlume.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test suite for CardDAO
 * Tests JDBC operations with ResultSet handling
 */
public class CardDAOTest extends BaseTest {
    private CardDAO cardDAO;
    private DeckDAO deckDAO;
    private UserDAO userDAO;
    private User testUser;
    private Deck testDeck;
    
    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        cardDAO = DAOFactory.createCardDAO();
        deckDAO = DAOFactory.createDeckDAO();
        userDAO = DAOFactory.createUserDAO();
        
        // Create test user and deck
        testUser = new User(generateUniqueUsername(), generateUniqueEmail(), 
                           BCrypt.hashpw("password123", BCrypt.gensalt()), 
                           "Test", "User");
        testUser = userDAO.create(testUser);
        testDeck = deckDAO.create(new Deck(testUser.getId(), "Test Deck", "Description"));
    }
    
    @Test
    public void testCreateCard() throws SQLException {
        // Arrange
        Card card = new Card(testDeck.getId(), "Front Text", "Back Text", Card.DifficultyLevel.MEDIUM);
        
        // Act
        Card createdCard = cardDAO.create(card);
        
        // Assert
        assertNotNull(createdCard);
        assertNotNull(createdCard.getId());
        assertEquals("Front Text", createdCard.getFrontText());
        assertEquals("Back Text", createdCard.getBackText());
        assertEquals(Card.DifficultyLevel.MEDIUM, createdCard.getDifficultyLevel());
        assertEquals(testDeck.getId(), createdCard.getDeckId());
    }
    
    @Test
    public void testFindCardById() throws SQLException {
        // Arrange
        Card card = new Card(testDeck.getId(), "Front", "Back", Card.DifficultyLevel.EASY);
        Card createdCard = cardDAO.create(card);
        
        // Act
        Card foundCard = cardDAO.findById(createdCard.getId());
        
        // Assert
        assertNotNull(foundCard);
        assertEquals(createdCard.getId(), foundCard.getId());
        assertEquals("Front", foundCard.getFrontText());
    }
    
    @Test
    public void testFindCardsByDeckId() throws SQLException {
        // Arrange
        cardDAO.create(new Card(testDeck.getId(), "Front 1", "Back 1", Card.DifficultyLevel.EASY));
        cardDAO.create(new Card(testDeck.getId(), "Front 2", "Back 2", Card.DifficultyLevel.MEDIUM));
        
        // Act
        List<Card> cards = cardDAO.findByDeckId(testDeck.getId());
        
        // Assert
        assertNotNull(cards);
        assertEquals(2, cards.size());
        assertTrue(cards.stream().allMatch(c -> c.getDeckId().equals(testDeck.getId())));
    }
    
    @Test
    public void testUpdateCard() throws SQLException {
        // Arrange
        Card card = new Card(testDeck.getId(), "Original Front", "Original Back", Card.DifficultyLevel.EASY);
        Card createdCard = cardDAO.create(card);
        
        // Act
        createdCard.setFrontText("Updated Front");
        createdCard.setBackText("Updated Back");
        createdCard.setDifficultyLevel(Card.DifficultyLevel.HARD);
        Card updatedCard = cardDAO.update(createdCard);
        
        // Assert
        assertNotNull(updatedCard);
        assertEquals("Updated Front", updatedCard.getFrontText());
        assertEquals("Updated Back", updatedCard.getBackText());
        assertEquals(Card.DifficultyLevel.HARD, updatedCard.getDifficultyLevel());
    }
    
    @Test
    public void testDeleteCard() throws SQLException {
        // Arrange
        Card card = new Card(testDeck.getId(), "Front", "Back", Card.DifficultyLevel.MEDIUM);
        Card createdCard = cardDAO.create(card);
        
        // Act
        boolean deleted = cardDAO.delete(createdCard.getId());
        
        // Assert
        assertTrue(deleted);
        Card deletedCard = cardDAO.findById(createdCard.getId());
        assertNull(deletedCard); // Should not find inactive card
    }
    
    @Test
    public void testFindAllCards() throws SQLException {
        // Arrange
        cardDAO.create(new Card(testDeck.getId(), "Front 1", "Back 1", Card.DifficultyLevel.EASY));
        cardDAO.create(new Card(testDeck.getId(), "Front 2", "Back 2", Card.DifficultyLevel.MEDIUM));
        
        // Act
        List<Card> cards = cardDAO.findAll();
        
        // Assert
        assertNotNull(cards);
        assertTrue(cards.size() >= 2);
    }
}

