package com.projectlume.dao;

import com.projectlume.factory.DAOFactory;
import com.projectlume.model.StudySession;
import com.projectlume.model.User;
import com.projectlume.model.Deck;
import com.projectlume.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test suite for StudySessionDAO
 * Tests JDBC operations for study sessions
 */
public class StudySessionDAOTest extends BaseTest {
    private StudySessionDAO studySessionDAO;
    private DeckDAO deckDAO;
    private UserDAO userDAO;
    private User testUser;
    private Deck testDeck;
    
    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        studySessionDAO = DAOFactory.createStudySessionDAO();
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
    public void testCreateStudySession() throws SQLException {
        // Arrange
        StudySession session = new StudySession(testUser.getId(), testDeck.getId());
        
        // Act
        StudySession createdSession = studySessionDAO.create(session);
        
        // Assert
        assertNotNull(createdSession);
        assertNotNull(createdSession.getId());
        assertEquals(testUser.getId(), createdSession.getUserId());
        assertEquals(testDeck.getId(), createdSession.getDeckId());
        assertEquals(0, createdSession.getCardsStudied());
    }
    
    @Test
    public void testFindStudySessionById() throws SQLException {
        // Arrange
        StudySession session = new StudySession(testUser.getId(), testDeck.getId());
        StudySession createdSession = studySessionDAO.create(session);
        
        // Act
        StudySession foundSession = studySessionDAO.findById(createdSession.getId());
        
        // Assert
        assertNotNull(foundSession);
        assertEquals(createdSession.getId(), foundSession.getId());
        assertEquals(testUser.getId(), foundSession.getUserId());
    }
    
    @Test
    public void testFindStudySessionsByUserId() throws SQLException {
        // Arrange
        studySessionDAO.create(new StudySession(testUser.getId(), testDeck.getId()));
        studySessionDAO.create(new StudySession(testUser.getId(), testDeck.getId()));
        
        // Act
        List<StudySession> sessions = studySessionDAO.findByUserId(testUser.getId());
        
        // Assert
        assertNotNull(sessions);
        assertTrue(sessions.size() >= 2);
        assertTrue(sessions.stream().allMatch(s -> s.getUserId().equals(testUser.getId())));
    }
    
    @Test
    public void testUpdateStudySession() throws SQLException {
        // Arrange
        StudySession session = new StudySession(testUser.getId(), testDeck.getId());
        StudySession createdSession = studySessionDAO.create(session);
        
        // Act
        createdSession.setCardsStudied(5);
        createdSession.setCorrectAnswers(4);
        createdSession.setIncorrectAnswers(1);
        createdSession.setSessionDurationMinutes(10);
        StudySession updatedSession = studySessionDAO.update(createdSession);
        
        // Assert
        assertNotNull(updatedSession);
        assertEquals(5, updatedSession.getCardsStudied());
        assertEquals(4, updatedSession.getCorrectAnswers());
        assertEquals(1, updatedSession.getIncorrectAnswers());
        assertEquals(10, updatedSession.getSessionDurationMinutes());
    }
}

