package com.projectlume.dto;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for deck statistics
 * Used to transfer aggregated deck statistics data to the view layer
 */
public class DeckStatsDTO {
    private final Long deckId;
    private final String name;
    private final String description;
    private final int totalCards;
    private final int cardsStudied;
    private final double completionPercentage;
    private final LocalDateTime lastStudyDate;
    private final Double averageAccuracy;

    /**
     * Constructor for DeckStatsDTO
     * @param deckId Deck ID
     * @param name Deck name
     * @param description Deck description (can be null)
     * @param totalCards Total number of cards in the deck
     * @param cardsStudied Total number of cards studied across all sessions
     * @param completionPercentage Percentage of cards studied (0-100)
     * @param lastStudyDate Most recent study session date (can be null)
     * @param averageAccuracy Average accuracy percentage across all sessions (can be null if no sessions)
     */
    public DeckStatsDTO(Long deckId, String name, String description, int totalCards, 
                       int cardsStudied, double completionPercentage, 
                       LocalDateTime lastStudyDate, Double averageAccuracy) {
        this.deckId = deckId;
        this.name = name;
        this.description = description;
        this.totalCards = totalCards;
        this.cardsStudied = cardsStudied;
        this.completionPercentage = completionPercentage;
        this.lastStudyDate = lastStudyDate;
        this.averageAccuracy = averageAccuracy;
    }

    public Long getDeckId() {
        return deckId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getTotalCards() {
        return totalCards;
    }

    public int getCardsStudied() {
        return cardsStudied;
    }

    public double getCompletionPercentage() {
        return completionPercentage;
    }

    public LocalDateTime getLastStudyDate() {
        return lastStudyDate;
    }

    public Double getAverageAccuracy() {
        return averageAccuracy;
    }

    @Override
    public String toString() {
        return "DeckStatsDTO{" +
                "deckId=" + deckId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", totalCards=" + totalCards +
                ", cardsStudied=" + cardsStudied +
                ", completionPercentage=" + completionPercentage +
                ", lastStudyDate=" + lastStudyDate +
                ", averageAccuracy=" + averageAccuracy +
                '}';
    }
}

