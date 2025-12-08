package com.projectlume.dao;

import com.projectlume.factory.DAOFactory;
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
 * Integration test suite for DeckDAO
 * Tests JDBC operations with real database connections
 */
public class DeckDAOTest extends BaseTest {
    private DeckDAO deckDAO;
    private UserDAO userDAO;
    private User testUser;
    
    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        deckDAO = DAOFactory.createDeckDAO();
        userDAO = DAOFactory.createUserDAO();
        
        // Create test user
        testUser = new User(generateUniqueUsername(), generateUniqueEmail(), 
                           BCrypt.hashpw("password123", BCrypt.gensalt()), 
                           "Test", "User");
        testUser = userDAO.create(testUser);
    }
    
    @Test
    public void testCreateDeck() throws SQLException {
        // Arrange
        Deck deck = new Deck(testUser.getId(), "Test Deck", "Description");
        
        // Act
        Deck createdDeck = deckDAO.create(deck);
        
        // Assert
        assertNotNull(createdDeck);
        assertNotNull(createdDeck.getId());
        assertEquals("Test Deck", createdDeck.getName());
        assertEquals(testUser.getId(), createdDeck.getUserId());
    }
    
    @Test
    public void testFindDeckById() throws SQLException {
        // Arrange
        Deck deck = new Deck(testUser.getId(), "Test Deck", "Description");
        Deck createdDeck = deckDAO.create(deck);
        
        // Act
        Deck foundDeck = deckDAO.findById(createdDeck.getId());
        
        // Assert
        assertNotNull(foundDeck);
        assertEquals(createdDeck.getId(), foundDeck.getId());
        assertEquals("Test Deck", foundDeck.getName());
    }
    
    @Test
    public void testFindDecksByUserId() throws SQLException {
        // Arrange
        deckDAO.create(new Deck(testUser.getId(), "Deck 1", "Description 1"));
        deckDAO.create(new Deck(testUser.getId(), "Deck 2", "Description 2"));
        
        // Act
        List<Deck> decks = deckDAO.findByUserId(testUser.getId());
        
        // Assert
        assertNotNull(decks);
        assertEquals(2, decks.size());
        assertTrue(decks.stream().allMatch(d -> d.getUserId().equals(testUser.getId())));
    }
    
    @Test
    public void testUpdateDeck() throws SQLException {
        // Arrange
        Deck deck = new Deck(testUser.getId(), "Original Name", "Original Description");
        Deck createdDeck = deckDAO.create(deck);
        
        // Act
        createdDeck.setName("Updated Name");
        createdDeck.setDescription("Updated Description");
        Deck updatedDeck = deckDAO.update(createdDeck);
        
        // Assert
        assertNotNull(updatedDeck);
        assertEquals("Updated Name", updatedDeck.getName());
        assertEquals("Updated Description", updatedDeck.getDescription());
    }
    
    @Test
    public void testDeleteDeck() throws SQLException {
        // Arrange
        Deck deck = new Deck(testUser.getId(), "To Delete", "Description");
        Deck createdDeck = deckDAO.create(deck);
        
        // Act
        boolean deleted = deckDAO.delete(createdDeck.getId());
        
        // Assert
        assertTrue(deleted);
        Deck deletedDeck = deckDAO.findById(createdDeck.getId());
        assertNull(deletedDeck); // Should not find inactive deck
    }
    
    @Test
    public void testGetCardCountForDeck() throws SQLException {
        // Arrange
        Deck deck = deckDAO.create(new Deck(testUser.getId(), "Test Deck", "Description"));
        CardDAO cardDAO = DAOFactory.createCardDAO();
        cardDAO.create(new com.projectlume.model.Card(deck.getId(), "Front 1", "Back 1", 
                                                      com.projectlume.model.Card.DifficultyLevel.EASY));
        cardDAO.create(new com.projectlume.model.Card(deck.getId(), "Front 2", "Back 2", 
                                                      com.projectlume.model.Card.DifficultyLevel.MEDIUM));
        
        // Act
        int cardCount = deckDAO.getCardCountForDeck(deck.getId());
        
        // Assert
        assertEquals(2, cardCount);
    }
}

