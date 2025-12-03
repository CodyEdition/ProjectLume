package com.projectlume.service;

import com.projectlume.dao.CardDAO;
import com.projectlume.dao.DeckDAO;
import com.projectlume.dao.StudySessionDAO;
import com.projectlume.dto.DeckStatsDTO;
import com.projectlume.factory.DAOFactory;
import com.projectlume.model.Card;
import com.projectlume.model.Deck;
import com.projectlume.model.StudySession;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

/**
 * Service for managing deck-related operations and statistics
 */
public class DeckService {
    private static final Logger logger = Logger.getLogger(DeckService.class.getName());
    private final DeckDAO deckDAO;
    private final CardDAO cardDAO;
    private final StudySessionDAO studySessionDAO;
    
    public DeckService() {
        this.deckDAO = DAOFactory.createDeckDAO();
        this.cardDAO = DAOFactory.createCardDAO();
        this.studySessionDAO = DAOFactory.createStudySessionDAO();
    }
    
    /**
     * Get deck statistics for all decks belonging to a user
     * Aggregates data from DeckDAO, CardDAO, and StudySessionDAO to create DeckStatsDTO objects
     * 
     * @param userId User ID
     * @return List of DeckStatsDTO objects containing statistics for each deck
     * @throws SQLException if database error occurs
     */
    public List<DeckStatsDTO> getDeckStatisticsForUser(Long userId) throws SQLException {
        List<DeckStatsDTO> deckStatsList = new ArrayList<>();
        
        // Get all decks for the user
        List<Deck> decks = deckDAO.findByUserId(userId);
        
        // For each deck, calculate statistics
        for (Deck deck : decks) {
            Long deckId = deck.getId();
            
            // Get total card count
            List<Card> cards = cardDAO.findByDeckId(deckId);
            int totalCards = cards.size();
            
            // Get study sessions for this deck
            List<StudySession> sessions = studySessionDAO.findByDeckId(deckId);
            
            // Calculate cards studied (sum of cards_studied from all sessions)
            int cardsStudied = sessions.stream()
                    .mapToInt(StudySession::getCardsStudied)
                    .sum();
            
            // Calculate completion percentage (handle division by zero)
            double completionPercentage = 0.0;
            if (totalCards > 0) {
                completionPercentage = (double) cardsStudied / totalCards * 100.0;
            }
            
            // Get last study date (most recent session_date)
            LocalDateTime lastStudyDate = null;
            if (!sessions.isEmpty()) {
                lastStudyDate = sessions.stream()
                        .map(StudySession::getSessionDate)
                        .max(Comparator.naturalOrder())
                        .orElse(null);
            }
            
            // Calculate average accuracy
            // Accuracy = (sum of correct_answers) / (sum of cards_studied) * 100
            Double averageAccuracy = null;
            if (!sessions.isEmpty() && cardsStudied > 0) {
                int totalCorrectAnswers = sessions.stream()
                        .mapToInt(StudySession::getCorrectAnswers)
                        .sum();
                averageAccuracy = (double) totalCorrectAnswers / cardsStudied * 100.0;
            }
            
            // Create DeckStatsDTO
            DeckStatsDTO deckStats = new DeckStatsDTO(
                    deckId,
                    deck.getName(),
                    deck.getDescription(),
                    totalCards,
                    cardsStudied,
                    completionPercentage,
                    deck.getCreatedAt(),
                    lastStudyDate,
                    averageAccuracy
            );
            
            deckStatsList.add(deckStats);
        }
        
        logger.info("Retrieved statistics for " + deckStatsList.size() + " decks for user " + userId);
        return deckStatsList;
    }
}

