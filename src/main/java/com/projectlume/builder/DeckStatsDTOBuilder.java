package com.projectlume.builder;

import com.projectlume.dto.DeckStatsDTO;

import java.time.LocalDateTime;

public class DeckStatsDTOBuilder {
    private Long deckId;
    private String name;
    private String description;
    private int totalCards;
    private int cardsStudied;
    private double completionPercentage;
    private LocalDateTime createdAt;
    private LocalDateTime lastStudyDate;
    private Double averageAccuracy;
    
    public DeckStatsDTOBuilder deckId(Long deckId) {
        this.deckId = deckId;
        return this;
    }
    
    public DeckStatsDTOBuilder name(String name) {
        this.name = name;
        return this;
    }
    
    public DeckStatsDTOBuilder description(String description) {
        this.description = description;
        return this;
    }
    
    public DeckStatsDTOBuilder totalCards(int totalCards) {
        this.totalCards = totalCards;
        return this;
    }
    
    public DeckStatsDTOBuilder cardsStudied(int cardsStudied) {
        this.cardsStudied = cardsStudied;
        return this;
    }
    
    public DeckStatsDTOBuilder completionPercentage(double completionPercentage) {
        this.completionPercentage = completionPercentage;
        return this;
    }
    
    public DeckStatsDTOBuilder createdAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }
    
    public DeckStatsDTOBuilder lastStudyDate(LocalDateTime lastStudyDate) {
        this.lastStudyDate = lastStudyDate;
        return this;
    }
    
    public DeckStatsDTOBuilder averageAccuracy(Double averageAccuracy) {
        this.averageAccuracy = averageAccuracy;
        return this;
    }
    
    public DeckStatsDTO build() {
        return new DeckStatsDTO(
            deckId,
            name,
            description,
            totalCards,
            cardsStudied,
            completionPercentage,
            createdAt,
            lastStudyDate,
            averageAccuracy
        );
    }
    
    public static DeckStatsDTOBuilder builder() {
        return new DeckStatsDTOBuilder();
    }
}

