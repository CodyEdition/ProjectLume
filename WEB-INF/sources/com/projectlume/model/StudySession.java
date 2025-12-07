package com.projectlume.model;

import java.time.LocalDateTime;

/**
 * StudySession entity representing a study session for a deck
 */
public class StudySession {
    private Long id;
    private Long userId;
    private Long deckId;
    private LocalDateTime sessionDate;
    private int cardsStudied;
    private int correctAnswers;
    private int incorrectAnswers;
    private int sessionDurationMinutes;
    
    // Default constructor
    public StudySession() {}
    
    // Constructor with basic fields
    public StudySession(Long userId, Long deckId) {
        this.userId = userId;
        this.deckId = deckId;
        this.sessionDate = LocalDateTime.now();
        this.cardsStudied = 0;
        this.correctAnswers = 0;
        this.incorrectAnswers = 0;
        this.sessionDurationMinutes = 0;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Long getDeckId() {
        return deckId;
    }
    
    public void setDeckId(Long deckId) {
        this.deckId = deckId;
    }
    
    public LocalDateTime getSessionDate() {
        return sessionDate;
    }
    
    public void setSessionDate(LocalDateTime sessionDate) {
        this.sessionDate = sessionDate;
    }
    
    public int getCardsStudied() {
        return cardsStudied;
    }
    
    public void setCardsStudied(int cardsStudied) {
        this.cardsStudied = cardsStudied;
    }
    
    public int getCorrectAnswers() {
        return correctAnswers;
    }
    
    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }
    
    public int getIncorrectAnswers() {
        return incorrectAnswers;
    }
    
    public void setIncorrectAnswers(int incorrectAnswers) {
        this.incorrectAnswers = incorrectAnswers;
    }
    
    public int getSessionDurationMinutes() {
        return sessionDurationMinutes;
    }
    
    public void setSessionDurationMinutes(int sessionDurationMinutes) {
        this.sessionDurationMinutes = sessionDurationMinutes;
    }
    
    @Override
    public String toString() {
        return "StudySession{" +
                "id=" + id +
                ", userId=" + userId +
                ", deckId=" + deckId +
                ", sessionDate=" + sessionDate +
                ", cardsStudied=" + cardsStudied +
                ", correctAnswers=" + correctAnswers +
                ", incorrectAnswers=" + incorrectAnswers +
                ", sessionDurationMinutes=" + sessionDurationMinutes +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudySession that = (StudySession) o;
        return id != null ? id.equals(that.id) : that.id == null;
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}

