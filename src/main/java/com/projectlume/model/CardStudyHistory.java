package com.projectlume.model;

import java.time.LocalDateTime;

/**
 * CardStudyHistory entity representing a study history entry for a card
 */
public class CardStudyHistory {
    private Long id;
    private Long cardId;
    private Long userId;
    private LocalDateTime studyDate;
    private boolean wasCorrect;
    private Integer responseTimeSeconds;
    
    // Default constructor
    public CardStudyHistory() {}
    
    // Constructor with basic fields
    public CardStudyHistory(Long cardId, Long userId, boolean wasCorrect) {
        this.cardId = cardId;
        this.userId = userId;
        this.wasCorrect = wasCorrect;
        this.studyDate = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getCardId() {
        return cardId;
    }
    
    public void setCardId(Long cardId) {
        this.cardId = cardId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public LocalDateTime getStudyDate() {
        return studyDate;
    }
    
    public void setStudyDate(LocalDateTime studyDate) {
        this.studyDate = studyDate;
    }
    
    public boolean isWasCorrect() {
        return wasCorrect;
    }
    
    public void setWasCorrect(boolean wasCorrect) {
        this.wasCorrect = wasCorrect;
    }
    
    public Integer getResponseTimeSeconds() {
        return responseTimeSeconds;
    }
    
    public void setResponseTimeSeconds(Integer responseTimeSeconds) {
        this.responseTimeSeconds = responseTimeSeconds;
    }
    
    @Override
    public String toString() {
        return "CardStudyHistory{" +
                "id=" + id +
                ", cardId=" + cardId +
                ", userId=" + userId +
                ", studyDate=" + studyDate +
                ", wasCorrect=" + wasCorrect +
                ", responseTimeSeconds=" + responseTimeSeconds +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CardStudyHistory that = (CardStudyHistory) o;
        return id != null ? id.equals(that.id) : that.id == null;
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}

