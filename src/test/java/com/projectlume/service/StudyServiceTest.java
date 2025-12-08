package com.projectlume.service;

import com.projectlume.dao.StudySessionDAO;
import com.projectlume.dto.StudySessionDTO;
import com.projectlume.factory.DAOFactory;
import com.projectlume.model.Card;
import com.projectlume.model.Deck;
import com.projectlume.model.StudySession;
import com.projectlume.model.User;
import com.projectlume.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for StudyService
 * Covers study session creation, answer recording, session completion, statistics calculation, and history retrieval
 */
public class StudyServiceTest extends BaseTest {
    private StudyService studyService;
    private CardService cardService;
    private DeckService deckService;
    private AuthService authService;
    private StudySessionDAO studySessionDAO;
    private User testUser;
    private User otherUser;
    private Deck testDeck;
    private Deck emptyDeck;
    private Card testCard1;
    private Card testCard2;
    
    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        studyService = new StudyService();
        cardService = new CardService();
        deckService = new DeckService();
        authService = new AuthService();
        studySessionDAO = DAOFactory.createStudySessionDAO();
        
        // Create test users and decks
        testUser = authService.register(generateUniqueUsername(), generateUniqueEmail(), 
                                       "TestPassword123!", "Test", "User");
        otherUser = authService.register(generateUniqueUsername(), generateUniqueEmail(), 
                                        "TestPassword123!", "Other", "User");
        
        testDeck = deckService.createDeck(testUser.getId(), "Test Deck", "Description");
        emptyDeck = deckService.createDeck(testUser.getId(), "Empty Deck", "Description");
        
        // Create test cards
        testCard1 = cardService.createCard(testUser.getId(), testDeck.getId(), 
                                           "Front 1", "Back 1", Card.DifficultyLevel.EASY);
        testCard2 = cardService.createCard(testUser.getId(), testDeck.getId(), 
                                           "Front 2", "Back 2", Card.DifficultyLevel.MEDIUM);
    }
    
    @Test
    public void testStartStudySessionWithValidDeck() throws SQLException {
        // Act
        StudySession session = studyService.startStudySession(testUser.getId(), testDeck.getId());
        
        // Assert
        assertNotNull(session);
        assertNotNull(session.getId());
        assertEquals(testUser.getId(), session.getUserId());
        assertEquals(testDeck.getId(), session.getDeckId());
        assertEquals(0, session.getCardsStudied());
        assertEquals(0, session.getCorrectAnswers());
        assertEquals(0, session.getIncorrectAnswers());
    }
    
    @Test
    public void testStartStudySessionWithEmptyDeck() throws SQLException {
        // Act
        StudySession session = studyService.startStudySession(testUser.getId(), emptyDeck.getId());
        
        // Assert - Should still create session even with empty deck
        assertNotNull(session);
        assertEquals(emptyDeck.getId(), session.getDeckId());
    }
    
    @Test
    public void testStartStudySessionWithNonOwnedDeck() throws SQLException {
        // Arrange
        Deck otherDeck = deckService.createDeck(otherUser.getId(), "Other Deck", "Description");
        
        // Act & Assert
        assertThrows(SQLException.class, () -> {
            studyService.startStudySession(testUser.getId(), otherDeck.getId());
        });
    }
    
    @Test
    public void testRecordCardAnswerCorrect() throws SQLException {
        // Arrange
        StudySession session = studyService.startStudySession(testUser.getId(), testDeck.getId());
        
        // Act
        studyService.recordCardAnswer(session.getId(), testCard1.getId(), testUser.getId(), true, null);
        
        // Assert
        StudySession updatedSession = studySessionDAO.findById(session.getId());
        assertEquals(1, updatedSession.getCardsStudied());
        assertEquals(1, updatedSession.getCorrectAnswers());
        assertEquals(0, updatedSession.getIncorrectAnswers());
    }
    
    @Test
    public void testRecordCardAnswerIncorrect() throws SQLException {
        // Arrange
        StudySession session = studyService.startStudySession(testUser.getId(), testDeck.getId());
        
        // Act
        studyService.recordCardAnswer(session.getId(), testCard1.getId(), testUser.getId(), false, null);
        
        // Assert
        StudySession updatedSession = studySessionDAO.findById(session.getId());
        assertEquals(1, updatedSession.getCardsStudied());
        assertEquals(0, updatedSession.getCorrectAnswers());
        assertEquals(1, updatedSession.getIncorrectAnswers());
    }
    
    @Test
    public void testRecordCardAnswerMultipleCards() throws SQLException {
        // Arrange
        StudySession session = studyService.startStudySession(testUser.getId(), testDeck.getId());
        
        // Act
        studyService.recordCardAnswer(session.getId(), testCard1.getId(), testUser.getId(), true, null);
        studyService.recordCardAnswer(session.getId(), testCard2.getId(), testUser.getId(), false, null);
        
        // Assert
        StudySession updatedSession = studySessionDAO.findById(session.getId());
        assertEquals(2, updatedSession.getCardsStudied());
        assertEquals(1, updatedSession.getCorrectAnswers());
        assertEquals(1, updatedSession.getIncorrectAnswers());
    }
    
    @Test
    public void testEndStudySession() throws SQLException {
        // Arrange
        StudySession session = studyService.startStudySession(testUser.getId(), testDeck.getId());
        studyService.recordCardAnswer(session.getId(), testCard1.getId(), testUser.getId(), true, null);
        int durationMinutes = 5;
        
        // Act
        StudySession endedSession = studyService.endStudySession(session.getId(), durationMinutes);
        
        // Assert
        assertNotNull(endedSession);
        assertEquals(durationMinutes, endedSession.getSessionDurationMinutes());
        assertEquals(1, endedSession.getCardsStudied());
    }
    
    @Test
    public void testGetCardsForDeck() throws SQLException {
        // Act
        List<Card> cards = studyService.getCardsForDeck(testUser.getId(), testDeck.getId());
        
        // Assert
        assertEquals(2, cards.size());
        assertTrue(cards.stream().anyMatch(c -> c.getId().equals(testCard1.getId())));
        assertTrue(cards.stream().anyMatch(c -> c.getId().equals(testCard2.getId())));
    }
    
    @Test
    public void testGetCardsForDeckNonOwned() throws SQLException {
        // Arrange
        Deck otherDeck = deckService.createDeck(otherUser.getId(), "Other Deck", "Description");
        
        // Act & Assert
        assertThrows(SecurityException.class, () -> {
            studyService.getCardsForDeck(testUser.getId(), otherDeck.getId());
        });
    }
    
    @Test
    public void testGetStudyHistoryForUser() throws SQLException {
        // Arrange
        StudySession session1 = studyService.startStudySession(testUser.getId(), testDeck.getId());
        studyService.recordCardAnswer(session1.getId(), testCard1.getId(), testUser.getId(), true, null);
        studyService.endStudySession(session1.getId(), 5);
        
        StudySession session2 = studyService.startStudySession(testUser.getId(), testDeck.getId());
        studyService.recordCardAnswer(session2.getId(), testCard2.getId(), testUser.getId(), false, null);
        studyService.endStudySession(session2.getId(), 3);
        
        // Act
        List<StudySessionDTO> history = studyService.getStudyHistoryForUser(testUser.getId());
        
        // Assert
        assertNotNull(history);
        assertTrue(history.size() >= 2);
    }
    
    @Test
    public void testGetStudyHistoryForUserWithNoSessions() throws SQLException {
        // Act
        List<StudySessionDTO> history = studyService.getStudyHistoryForUser(testUser.getId());
        
        // Assert
        assertNotNull(history);
        assertTrue(history.isEmpty());
    }
}

