package com.projectlume.service;

import com.projectlume.builder.StudySessionDTOBuilder;
import com.projectlume.dao.CardDAO;
import com.projectlume.dao.CardStudyHistoryDAO;
import com.projectlume.dao.DeckDAO;
import com.projectlume.dao.StudySessionDAO;
import com.projectlume.dto.StudySessionDTO;
import com.projectlume.factory.DAOFactory;
import com.projectlume.model.Card;
import com.projectlume.model.CardStudyHistory;
import com.projectlume.model.Deck;
import com.projectlume.model.StudySession;
import com.projectlume.observer.EventPublisher;
import com.projectlume.observer.impl.StudySessionCompletedEvent;
import com.projectlume.util.ThreadPoolManager;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Service for managing study sessions and card study history
 * Refactored to extend BaseService and use Observer pattern for events
 */
public class StudyService extends BaseService {
    private final StudySessionDAO studySessionDAO;
    private final CardStudyHistoryDAO cardStudyHistoryDAO;
    private final DeckDAO deckDAO;
    private final CardDAO cardDAO;
    
    public StudyService() {
        super();
        this.studySessionDAO = DAOFactory.createStudySessionDAO();
        this.cardStudyHistoryDAO = DAOFactory.createCardStudyHistoryDAO();
        this.deckDAO = DAOFactory.createDeckDAO();
        this.cardDAO = DAOFactory.createCardDAO();
    }
    
    /**
     * Start a new study session for a deck
     * @param userId User ID
     * @param deckId Deck ID
     * @return Created StudySession with generated ID
     * @throws SQLException if database error occurs or user doesn't own deck
     */
    public StudySession startStudySession(Long userId, Long deckId) throws SQLException {
        // Validate deck ownership
        Deck deck = deckDAO.findById(deckId);
        if (deck == null) {
            throw new SQLException("Deck not found");
        }
        if (!deck.getUserId().equals(userId)) {
            throw new SQLException("User does not own this deck");
        }
        
        // Create new study session using template method from BaseService
        StudySession session = new StudySession(userId, deckId);
        session.setCardsStudied(0);
        session.setCorrectAnswers(0);
        session.setIncorrectAnswers(0);
        session.setSessionDurationMinutes(0);
        
        StudySession createdSession = executeCreate(() -> studySessionDAO.create(session));
        logger.info("Study session started: ID " + createdSession.getId() + " for deck " + deckId);
        return createdSession;
    }
    
    /**
     * Record a card answer and update session statistics
     * @param sessionId Study session ID
     * @param cardId Card ID
     * @param userId User ID
     * @param wasCorrect Whether the answer was correct
     * @param responseTimeSeconds Response time in seconds (can be null)
     * @throws SQLException if database error occurs
     */
    public void recordCardAnswer(Long sessionId, Long cardId, Long userId, boolean wasCorrect, Integer responseTimeSeconds) throws SQLException {
        // Get current session
        StudySession session = studySessionDAO.findById(sessionId);
        if (session == null) {
            throw new SQLException("Study session not found");
        }
        if (!session.getUserId().equals(userId)) {
            throw new SQLException("User does not own this study session");
        }
        
        // Verify card exists and belongs to the session's deck
        com.projectlume.model.Card card = cardDAO.findById(cardId);
        if (card == null) {
            throw new SQLException("Card not found");
        }
        if (!card.getDeckId().equals(session.getDeckId())) {
            throw new SQLException("Card does not belong to the study session's deck");
        }
        
        // Create card study history entry
        CardStudyHistory history = new CardStudyHistory(cardId, userId, wasCorrect);
        history.setResponseTimeSeconds(responseTimeSeconds);
        cardStudyHistoryDAO.create(history);
        
        // Update session statistics
        session.setCardsStudied(session.getCardsStudied() + 1);
        if (wasCorrect) {
            session.setCorrectAnswers(session.getCorrectAnswers() + 1);
        } else {
            session.setIncorrectAnswers(session.getIncorrectAnswers() + 1);
        }
        studySessionDAO.update(session);
        
        logger.info("Card answer recorded: Card ID " + cardId + ", Correct: " + wasCorrect);
    }
    
    /**
     * End a study session and update duration
     * Uses multi-threading for async background operations
     * @param sessionId Study session ID
     * @param durationMinutes Session duration in minutes
     * @return Completed StudySession
     * @throws SQLException if database error occurs
     */
    public StudySession endStudySession(Long sessionId, int durationMinutes) throws SQLException {
        StudySession session = studySessionDAO.findById(sessionId);
        if (session == null) {
            throw new SQLException("Study session not found");
        }
        
        session.setSessionDurationMinutes(durationMinutes);
        StudySession updatedSession = executeUpdate(() -> studySessionDAO.update(session));
        
        // Use ExecutorService for async background task (multi-threading)
        ExecutorService executor = ThreadPoolManager.getCachedThreadPool();
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    // This runs in a separate thread from the thread pool
                    logger.info("Processing study session completion in thread: " + Thread.currentThread().getName());
                    
                    // Publish event using Observer pattern (async)
                    EventPublisher.getInstance().publish(
                        new StudySessionCompletedEvent(updatedSession, session.getUserId(), session.getDeckId())
                    );
                    
                    // Background cleanup of old study sessions (async operation)
                    cleanupOldStudySessions(session.getUserId());
                    
                } catch (Exception e) {
                    logger.warning("Error in async study session completion task: " + e.getMessage());
                }
            }
        });
        
        logger.info("Study session ended: ID " + sessionId + ", Duration: " + durationMinutes + " minutes");
        return updatedSession;
    }
    
    /**
     * Background cleanup of old study sessions (async operation)
     * Demonstrates multi-threading for background tasks
     */
    private void cleanupOldStudySessions(Long userId) {
        try {
            // This is a background task that runs asynchronously
            logger.info("Cleaning up old study sessions for user: " + userId);
            // In a real application, this would delete sessions older than a certain date
            // For now, just log that the cleanup task ran
            logger.fine("Study session cleanup completed for user: " + userId);
        } catch (Exception e) {
            logger.warning("Error during study session cleanup: " + e.getMessage());
        }
    }
    
    /**
     * Get statistics for a study session
     * @param sessionId Study session ID
     * @return StudySession object with statistics
     * @throws SQLException if database error occurs
     */
    public StudySession getSessionStats(Long sessionId) throws SQLException {
        StudySession session = studySessionDAO.findById(sessionId);
        if (session == null) {
            throw new SQLException("Study session not found");
        }
        return session;
    }
    
    /**
     * Get deck for a user, validating ownership
     * @param userId User ID
     * @param deckId Deck ID
     * @return Deck object
     * @throws SQLException if database error occurs
     * @throws SecurityException if user doesn't own the deck
     */
    public Deck getDeckForUser(Long userId, Long deckId) throws SQLException {
        Deck deck = deckDAO.findById(deckId);
        if (deck == null) {
            throw new SQLException("Deck not found");
        }
        if (!deck.getUserId().equals(userId)) {
            throw new SecurityException("User does not own this deck");
        }
        return deck;
    }
    
    /**
     * Get cards for a deck, validating that the user owns the deck
     * @param userId User ID
     * @param deckId Deck ID
     * @return List of cards belonging to the deck
     * @throws SQLException if database error occurs
     * @throws SecurityException if user doesn't own the deck
     */
    public List<Card> getCardsForDeck(Long userId, Long deckId) throws SQLException {
        // Validate deck ownership
        Deck deck = deckDAO.findById(deckId);
        if (deck == null) {
            throw new SQLException("Deck not found");
        }
        if (!deck.getUserId().equals(userId)) {
            throw new SecurityException("User does not own this deck");
        }
        
        return cardDAO.findByDeckId(deckId);
    }
    
    /**
     * Get study history for a user, converting sessions to DTOs with formatted data
     * @param userId User ID
     * @return List of StudySessionDTO objects containing formatted study session data
     * @throws SQLException if database error occurs
     */
    public List<StudySessionDTO> getStudyHistoryForUser(Long userId) throws SQLException {
        List<StudySessionDTO> dtoList = new ArrayList<>();
        
        // Get all study sessions for the user
        List<StudySession> sessions = studySessionDAO.findByUserId(userId);
        
        // Date formatter for display
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd, h:mma");
        
        // Convert each session to DTO
        for (StudySession session : sessions) {
            // Format date
            LocalDateTime date = session.getSessionDate();
            String formattedDate = (date != null) ? dateFormatter.format(date) : "";
            
            // Get deck name
            Long deckId = session.getDeckId();
            Deck deck = null;
            try {
                deck = deckDAO.findById(deckId);
            } catch (SQLException e) {
                logger.warning("Error fetching deck " + deckId + " for study history: " + e.getMessage());
            }
            String deckName = (deck != null) ? deck.getName() : "";
            
            // Get study count
            int studyCount = session.getCardsStudied();
            
            // Calculate accuracy
            int correctAnswers = session.getCorrectAnswers();
            int incorrectAnswers = session.getIncorrectAnswers();
            int total = correctAnswers + incorrectAnswers;
            float accuracy = (total == 0) ? 0 : ((float) 100 * correctAnswers / total);
            String formattedAccuracy = String.format("%.2f%%", accuracy);
            
            // Create DTO using Builder pattern
            StudySessionDTO dto = StudySessionDTOBuilder.builder()
                    .formattedDate(formattedDate)
                    .name(deckName)
                    .studyCount(studyCount)
                    .formattedAccuracy(formattedAccuracy)
                    .build();
            dtoList.add(dto);
        }
        
        logger.info("Retrieved study history for user " + userId + ": " + dtoList.size() + " sessions");
        return dtoList;
    }
}

