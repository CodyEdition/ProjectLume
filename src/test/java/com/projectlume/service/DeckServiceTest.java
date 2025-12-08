package com.projectlume.service;

import com.projectlume.dao.DeckDAO;
import com.projectlume.dto.DeckStatsDTO;
import com.projectlume.factory.DAOFactory;
import com.projectlume.model.Deck;
import com.projectlume.model.User;
import com.projectlume.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for DeckService
 */
public class DeckServiceTest extends BaseTest {
    private DeckService deckService;
    private AuthService authService;
    private DeckDAO deckDAO;
    private User testUser;
    private User otherUser;
    
    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        deckService = new DeckService();
        authService = new AuthService();
        deckDAO = DAOFactory.createDeckDAO();
        
        testUser = authService.register(generateUniqueUsername(), generateUniqueEmail(), 
                                       "TestPassword123!", "Test", "User");
        otherUser = authService.register(generateUniqueUsername(), generateUniqueEmail(), 
                                        "TestPassword123!", "Other", "User");
    }
    
    @Test
    public void testCreateDeckWithValidData() throws SQLException {
        String deckName = "Test Deck";
        String description = "Test Description";
        
        Deck createdDeck = deckService.createDeck(testUser.getId(), deckName, description);
        
        assertNotNull(createdDeck);
        assertNotNull(createdDeck.getId());
        assertEquals(deckName, createdDeck.getName());
        assertEquals(description, createdDeck.getDescription());
        assertEquals(testUser.getId(), createdDeck.getUserId());
        assertTrue(createdDeck.isActive());
    }
    
    @Test
    public void testCreateDeckWithEmptyName() {
        assertThrows(IllegalArgumentException.class, () -> {
            deckService.createDeck(testUser.getId(), "", "Description");
        });
    }
    
    @Test
    public void testCreateDeckWithNullName() {
        assertThrows(IllegalArgumentException.class, () -> {
            deckService.createDeck(testUser.getId(), null, "Description");
        });
    }
    
    @Test
    public void testGetDeckForUserOwnedDeck() throws SQLException {
        Deck deck = deckService.createDeck(testUser.getId(), "My Deck", "Description");
        
        Deck retrievedDeck = deckService.getDeckForUser(testUser.getId(), deck.getId());
        
        assertNotNull(retrievedDeck);
        assertEquals(deck.getId(), retrievedDeck.getId());
        assertEquals(deck.getName(), retrievedDeck.getName());
    }
    
    @Test
    public void testGetDeckForUserNonOwnedDeck() throws SQLException {
        Deck deck = deckService.createDeck(testUser.getId(), "My Deck", "Description");
        
        assertThrows(SecurityException.class, () -> {
            deckService.getDeckForUser(otherUser.getId(), deck.getId());
        });
    }
    
    @Test
    public void testUpdateDeckWithValidData() throws SQLException {
        Deck deck = deckService.createDeck(testUser.getId(), "Original Name", "Original Description");
        String newName = "Updated Name";
        String newDescription = "Updated Description";
        
        Deck updatedDeck = deckService.updateDeck(testUser.getId(), deck.getId(), newName, newDescription);
        
        assertNotNull(updatedDeck);
        assertEquals(newName, updatedDeck.getName());
        assertEquals(newDescription, updatedDeck.getDescription());
        assertEquals(deck.getId(), updatedDeck.getId());
    }
    
    @Test
    public void testUpdateDeckWithInvalidName() throws SQLException {
        Deck deck = deckService.createDeck(testUser.getId(), "Original Name", "Description");
        
        assertThrows(IllegalArgumentException.class, () -> {
            deckService.updateDeck(testUser.getId(), deck.getId(), "", "Description");
        });
    }
    
    @Test
    public void testUpdateDeckNonOwned() throws SQLException {
        Deck deck = deckService.createDeck(testUser.getId(), "My Deck", "Description");
        
        assertThrows(SecurityException.class, () -> {
            deckService.updateDeck(otherUser.getId(), deck.getId(), "Hacked Name", "Description");
        });
    }
    
    @Test
    public void testDeleteDeck() throws SQLException {
        Deck deck = deckService.createDeck(testUser.getId(), "To Delete", "Description");
        
        boolean deleted = deckService.deleteDeck(testUser.getId(), deck.getId());
        
        assertTrue(deleted);
        Deck deletedDeck = deckDAO.findById(deck.getId());
        assertNull(deletedDeck);
    }
    
    @Test
    public void testDeleteDeckNonOwned() throws SQLException {
        Deck deck = deckService.createDeck(testUser.getId(), "My Deck", "Description");
        
        assertThrows(SecurityException.class, () -> {
            deckService.deleteDeck(otherUser.getId(), deck.getId());
        });
    }
    
    @Test
    public void testGetDecksForUser() throws SQLException {
        deckService.createDeck(testUser.getId(), "Deck 1", "Description 1");
        deckService.createDeck(testUser.getId(), "Deck 2", "Description 2");
        deckService.createDeck(otherUser.getId(), "Other Deck", "Description");
        
        List<Deck> userDecks = deckService.getDecksForUser(testUser.getId());
        
        assertEquals(2, userDecks.size());
        assertTrue(userDecks.stream().allMatch(d -> d.getUserId().equals(testUser.getId())));
    }
    
    @Test
    public void testGetDeckStatisticsForUser() throws SQLException {
        Deck deck = deckService.createDeck(testUser.getId(), "Test Deck", "Description");
        
        List<DeckStatsDTO> stats = deckService.getDeckStatisticsForUser(testUser.getId());
        
        assertNotNull(stats);
        assertEquals(1, stats.size());
        DeckStatsDTO deckStats = stats.get(0);
        assertEquals(deck.getId(), deckStats.getDeckId());
        assertEquals(0, deckStats.getTotalCards()); // No cards yet
        assertEquals(0, deckStats.getCardsStudied());
        assertEquals(0.0, deckStats.getCompletionPercentage());
    }
    
    @Test
    public void testGetDeckStatisticsForUserWithNoDecks() throws SQLException {
        List<DeckStatsDTO> stats = deckService.getDeckStatisticsForUser(testUser.getId());
        
        assertNotNull(stats);
        assertTrue(stats.isEmpty());
    }
}

