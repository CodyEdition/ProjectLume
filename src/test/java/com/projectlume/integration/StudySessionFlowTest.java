package com.projectlume.integration;

import com.projectlume.dto.StudySessionDTO;
import com.projectlume.model.Card;
import com.projectlume.model.Deck;
import com.projectlume.model.StudySession;
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
 * End-to-end integration test for study session flow
 * Tests study session start → answer recording → completion → history display
 */
public class StudySessionFlowTest extends BaseTest {
    private StudyService studyService;
    private CardService cardService;
    private DeckService deckService;
    private AuthService authService;
    private User testUser;
    private Deck testDeck;
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
        
        testUser = authService.register(generateUniqueUsername(), generateUniqueEmail(), 
                                       "TestPassword123!", "Test", "User");
        testDeck = deckService.createDeck(testUser.getId(), "Test Deck", "Description");
        testCard1 = cardService.createCard(testUser.getId(), testDeck.getId(), 
                                           "Front 1", "Back 1", Card.DifficultyLevel.EASY);
        testCard2 = cardService.createCard(testUser.getId(), testDeck.getId(), 
                                           "Front 2", "Back 2", Card.DifficultyLevel.MEDIUM);
    }
    
    @Test
    public void testCompleteStudySessionFlow() throws SQLException {
        // Act - Start session
        StudySession session = studyService.startStudySession(testUser.getId(), testDeck.getId());
        
        // Assert - Session started
        assertNotNull(session);
        assertNotNull(session.getId());
        assertEquals(0, session.getCardsStudied());
        
        // Act - Record answers
        studyService.recordCardAnswer(session.getId(), testCard1.getId(), testUser.getId(), true, null);
        studyService.recordCardAnswer(session.getId(), testCard2.getId(), testUser.getId(), false, null);
        
        // Act - End session
        StudySession endedSession = studyService.endStudySession(session.getId(), 10);
        
        // Assert - Session completed
        assertEquals(2, endedSession.getCardsStudied());
        assertEquals(1, endedSession.getCorrectAnswers());
        assertEquals(1, endedSession.getIncorrectAnswers());
        assertEquals(10, endedSession.getSessionDurationMinutes());
    }
    
    @Test
    public void testStartStudySessionWithEmptyDeck() throws SQLException {
        // Arrange
        Deck emptyDeck = deckService.createDeck(testUser.getId(), "Empty Deck", "Description");
        
        // Act
        StudySession session = studyService.startStudySession(testUser.getId(), emptyDeck.getId());
        
        // Assert - Should still create session
        assertNotNull(session);
        assertEquals(emptyDeck.getId(), session.getDeckId());
    }
    
    @Test
    public void testStudySessionWithNonOwnedDeck() throws Exception {
        // Arrange
        User otherUser = authService.register(generateUniqueUsername(), generateUniqueEmail(), 
                                             "TestPassword123!", "Other", "User");
        Deck otherDeck = deckService.createDeck(otherUser.getId(), "Other Deck", "Description");
        
        // Act & Assert
        assertThrows(SQLException.class, () -> {
            studyService.startStudySession(testUser.getId(), otherDeck.getId());
        });
    }
    
    @Test
    public void testStudyHistoryRetrieval() throws SQLException {
        // Arrange - Create multiple study sessions
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
        
        // Verify date formatting
        for (StudySessionDTO dto : history) {
            assertNotNull(dto.getDate());
            assertNotNull(dto.getName());
            assertTrue(dto.getStudyCount() >= 0);
            assertNotNull(dto.getFormattedAccuracy());
        }
    }
    
    @Test
    public void testStudyHistoryWithEmptyHistory() throws SQLException {
        // Act
        List<StudySessionDTO> history = studyService.getStudyHistoryForUser(testUser.getId());
        
        // Assert
        assertNotNull(history);
        assertTrue(history.isEmpty());
    }
    
    @Test
    public void testStudyHistoryAccuracyCalculation() throws SQLException {
        // Arrange - Create session with known correct/incorrect answers
        StudySession session = studyService.startStudySession(testUser.getId(), testDeck.getId());
        studyService.recordCardAnswer(session.getId(), testCard1.getId(), testUser.getId(), true, null);
        studyService.recordCardAnswer(session.getId(), testCard2.getId(), testUser.getId(), true, null);
        studyService.endStudySession(session.getId(), 5);
        
        // Act
        List<StudySessionDTO> history = studyService.getStudyHistoryForUser(testUser.getId());
        
        // Assert
        assertNotNull(history);
        assertFalse(history.isEmpty());
        
        // Find the session we just created
        StudySessionDTO sessionDTO = history.stream()
            .filter(dto -> dto.getStudyCount() == 2)
            .findFirst()
            .orElse(null);
        
        assertNotNull(sessionDTO);
        // Accuracy should be 100% (2 correct out of 2)
        assertTrue(sessionDTO.getFormattedAccuracy().contains("100") || 
                  sessionDTO.getFormattedAccuracy().equals("100%"));
    }
    
    @Test
    public void testStudyHistoryWithNonOwnedDeck() throws Exception {
        // Arrange
        User otherUser = authService.register(generateUniqueUsername(), generateUniqueEmail(), 
                                             "TestPassword123!", "Other", "User");
        Deck otherDeck = deckService.createDeck(otherUser.getId(), "Other Deck", "Description");
        
        // Act & Assert
        assertThrows(SQLException.class, () -> {
            studyService.startStudySession(testUser.getId(), otherDeck.getId());
        });
    }
}

