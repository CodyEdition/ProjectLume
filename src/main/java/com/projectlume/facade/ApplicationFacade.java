package com.projectlume.facade;

import com.projectlume.dto.DeckStatsDTO;
import com.projectlume.dto.StudySessionDTO;
import com.projectlume.model.Card;
import com.projectlume.model.Deck;
import com.projectlume.model.StudySession;
import com.projectlume.model.User;
import com.projectlume.service.AuthService;
import com.projectlume.service.CardService;
import com.projectlume.service.DeckService;
import com.projectlume.service.StudyService;

import java.sql.SQLException;
import java.util.List;

public class ApplicationFacade {
    private final AuthService authService;
    private final DeckService deckService;
    private final CardService cardService;
    private final StudyService studyService;
    
    public ApplicationFacade(AuthService authService, DeckService deckService, 
                           CardService cardService, StudyService studyService) {
        this.authService = authService;
        this.deckService = deckService;
        this.cardService = cardService;
        this.studyService = studyService;
    }
    
    public User registerUser(String username, String email, String password, 
                            String firstName, String lastName) throws AuthService.AuthException {
        return authService.register(username, email, password, firstName, lastName);
    }
    
    public User loginUser(String username, String password) throws AuthService.AuthException {
        return authService.login(username, password);
    }
    
    public User updateUserProfile(Long userId, String username, String email, 
                                 String firstName, String lastName) throws Exception {
        return authService.updateProfile(userId, username, email, firstName, lastName);
    }
    
    public List<DeckStatsDTO> getDeckStatistics(Long userId) throws SQLException {
        return deckService.getDeckStatisticsForUser(userId);
    }
    
    public List<Deck> getUserDecks(Long userId) throws SQLException {
        return deckService.getDecksForUser(userId);
    }
    
    public Deck getDeck(Long userId, Long deckId) throws SQLException {
        return deckService.getDeckForUser(userId, deckId);
    }
    
    public Deck createDeck(Long userId, String name, String description) throws SQLException {
        return deckService.createDeck(userId, name, description);
    }
    
    public Deck updateDeck(Long userId, Long deckId, String name, String description) throws SQLException {
        return deckService.updateDeck(userId, deckId, name, description);
    }
    
    public boolean deleteDeck(Long userId, Long deckId) throws SQLException {
        return deckService.deleteDeck(userId, deckId);
    }
    
    public List<Card> getDeckCards(Long userId, Long deckId) throws SQLException {
        return cardService.getCardsForDeck(userId, deckId);
    }
    
    public Card getCard(Long userId, Long cardId) throws SQLException {
        return cardService.getCardForUser(userId, cardId);
    }
    
    public Card createCard(Long userId, Long deckId, String frontText, String backText, 
                          Card.DifficultyLevel difficultyLevel) throws SQLException {
        return cardService.createCard(userId, deckId, frontText, backText, difficultyLevel);
    }
    
    public Card updateCard(Long userId, Long cardId, String frontText, String backText, 
                          Card.DifficultyLevel difficultyLevel) throws SQLException {
        return cardService.updateCard(userId, cardId, frontText, backText, difficultyLevel);
    }
    
    public boolean deleteCard(Long userId, Long cardId) throws SQLException {
        return cardService.deleteCard(userId, cardId);
    }
    
    public StudySession startStudySession(Long userId, Long deckId) throws SQLException {
        return studyService.startStudySession(userId, deckId);
    }
    
    public void recordCardAnswer(Long sessionId, Long cardId, Long userId, 
                                boolean wasCorrect, Integer responseTimeSeconds) throws SQLException {
        studyService.recordCardAnswer(sessionId, cardId, userId, wasCorrect, responseTimeSeconds);
    }
    
    public StudySession endStudySession(Long sessionId, int durationMinutes) throws SQLException {
        return studyService.endStudySession(sessionId, durationMinutes);
    }
    
    public List<StudySessionDTO> getStudyHistory(Long userId) throws SQLException {
        return studyService.getStudyHistoryForUser(userId);
    }
    
    public List<Card> getStudyCards(Long userId, Long deckId) throws SQLException {
        return studyService.getCardsForDeck(userId, deckId);
    }
}

