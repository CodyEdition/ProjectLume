package com.projectlume.model;

import java.time.LocalDateTime;

/**
 * Card entity representing an individual flashcard
 */
public class Card {
    private Long id;
    private Long deckId;
    private String frontText;
    private String backText;
    private DifficultyLevel difficultyLevel;
    private boolean completed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean active;
    
    // Enum for difficulty levels
    public enum DifficultyLevel {
        EASY, MEDIUM, HARD
    }
    
    // Default constructor
    public Card() {}
    
    // Constructor with basic fields
    public Card(Long deckId, String frontText, String backText, DifficultyLevel difficultyLevel) {
        this.deckId = deckId;
        this.frontText = frontText;
        this.backText = backText;
        this.difficultyLevel = difficultyLevel != null ? difficultyLevel : DifficultyLevel.MEDIUM;
        this.active = true;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getDeckId() {
        return deckId;
    }
    
    public void setDeckId(Long deckId) {
        this.deckId = deckId;
    }
    
    public String getFrontText() {
        return frontText;
    }
    
    public void setFrontText(String frontText) {
        this.frontText = frontText;
    }
    
    public String getBackText() {
        return backText;
    }
    
    public void setBackText(String backText) {
        this.backText = backText;
    }
    
    public DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }
    
    public void setDifficultyLevel(DifficultyLevel difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }
    
    public boolean isCompleted() {
        return completed;
    }
    
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
    
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    @Override
    public String toString() {
        return "Card{" +
                "id=" + id +
                ", deckId=" + deckId +
                ", frontText='" + frontText + '\'' +
                ", backText='" + backText + '\'' +
                ", difficultyLevel=" + difficultyLevel +
                ", completed=" + completed +
                
                ", active=" + active +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return id != null ? id.equals(card.id) : card.id == null;
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
