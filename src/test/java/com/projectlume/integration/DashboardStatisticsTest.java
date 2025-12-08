package com.projectlume.integration;

import com.projectlume.dto.DeckStatsDTO;
import com.projectlume.model.Card;
import com.projectlume.model.Deck;
import com.projectlume.model.User;
import com.projectlume.service.AuthService;
import com.projectlume.service.CardService;
import com.projectlume.service.DeckService;
import com.projectlume.service.StudyService;
import com.projectlume.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for dashboard statistics
 * Tests deck statistics calculation, formatting, and display
 */
public class DashboardStatisticsTest extends BaseTest {
    private DeckService deckService;
    private CardService cardService;
    private StudyService studyService;
    private AuthService authService;
    private User testUser;
    
    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        deckService = new DeckService();
        cardService = new CardService();
        studyService = new StudyService();
        authService = new AuthService();
        
        testUser = authService.register(generateUniqueUsername(), generateUniqueEmail(), 
                                       "TestPassword123!", "Test", "User");
    }
    
    @Test
    public void testDashboardWithMultipleDecks() throws SQLException {
        // Arrange
        Deck deck1 = deckService.createDeck(testUser.getId(), "Deck 1", "Description 1");
        deckService.createDeck(testUser.getId(), "Deck 2", "Description 2");
        
        // Add cards to deck1
        cardService.createCard(testUser.getId(), deck1.getId(), "Front 1", "Back 1", null);
        cardService.createCard(testUser.getId(), deck1.getId(), "Front 2", "Back 2", null);
        
        // Act
        List<DeckStatsDTO> stats = deckService.getDeckStatisticsForUser(testUser.getId());
        
        // Assert
        assertNotNull(stats);
        assertEquals(2, stats.size());
        
        DeckStatsDTO deck1Stats = stats.stream()
            .filter(s -> s.getDeckId().equals(deck1.getId()))
            .findFirst()
            .orElse(null);
        assertNotNull(deck1Stats);
        assertEquals(2, deck1Stats.getTotalCards());
    }
    
    @Test
    public void testDashboardWithEmptyDeck() throws SQLException {
        // Arrange
        deckService.createDeck(testUser.getId(), "Empty Deck", "Description");
        
        // Act
        List<DeckStatsDTO> stats = deckService.getDeckStatisticsForUser(testUser.getId());
        
        // Assert
        assertNotNull(stats);
        assertEquals(1, stats.size());
        DeckStatsDTO deckStats = stats.get(0);
        assertEquals(0, deckStats.getTotalCards());
        assertEquals(0, deckStats.getCardsStudied());
        assertEquals(0.0, deckStats.getCompletionPercentage());
    }
    
    @Test
    public void testDashboardWithNoDecks() throws SQLException {
        // Act
        List<DeckStatsDTO> stats = deckService.getDeckStatisticsForUser(testUser.getId());
        
        // Assert
        assertNotNull(stats);
        assertTrue(stats.isEmpty());
    }
    
    @Test
    public void testDashboardStatisticsWithStudySessions() throws SQLException {
        // Arrange
        Deck deck = deckService.createDeck(testUser.getId(), "Test Deck", "Description");
        Card card1 = cardService.createCard(testUser.getId(), deck.getId(), "Front 1", "Back 1", null);
        Card card2 = cardService.createCard(testUser.getId(), deck.getId(), "Front 2", "Back 2", null);
        
        // Create study session
        com.projectlume.model.StudySession session = studyService.startStudySession(testUser.getId(), deck.getId());
        studyService.recordCardAnswer(session.getId(), card1.getId(), testUser.getId(), true, null);
        studyService.recordCardAnswer(session.getId(), card2.getId(), testUser.getId(), false, null);
        studyService.endStudySession(session.getId(), 5);
        
        // Act
        List<DeckStatsDTO> stats = deckService.getDeckStatisticsForUser(testUser.getId());
        
        // Assert
        assertNotNull(stats);
        assertEquals(1, stats.size());
        DeckStatsDTO deckStats = stats.get(0);
        assertEquals(2, deckStats.getTotalCards());
        assertTrue(deckStats.getCardsStudied() > 0);
        assertNotNull(deckStats.getLastStudyDate());
    }
}

