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
    private final LocalDateTime createdAt;
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
     * @param createdAt Deck creation date
     * @param lastStudyDate Most recent study session date (can be null)
     * @param averageAccuracy Average accuracy percentage across all sessions (can be null if no sessions)
     */
    public DeckStatsDTO(Long deckId, String name, String description, int totalCards, 
                       int cardsStudied, double completionPercentage, 
                       LocalDateTime createdAt, LocalDateTime lastStudyDate, Double averageAccuracy) {
        this.deckId = deckId;
        this.name = name;
        this.description = description;
        this.totalCards = totalCards;
        this.cardsStudied = cardsStudied;
        this.completionPercentage = completionPercentage;
        this.createdAt = createdAt;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getLastStudyDate() {
        return lastStudyDate;
    }

    public Double getAverageAccuracy() {
        return averageAccuracy;
    }
    
    /**
     * Get formatted date code for display (e.g., "NOV10.25.10")
     * Format: MONTHDAY.YEARS.DAY
     * @return Formatted date code string, or "NOV10.25.10" as default if createdAt is null
     */
    public String getDateCode() {
        if (createdAt == null) {
            return "NOV10.25.10";
        }
        
        String[] monthNames = {"JAN", "FEB", "MAR", "APR", "MAY", "JUN", 
                              "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};
        String month = monthNames[createdAt.getMonthValue() - 1];
        String day = String.format("%02d", createdAt.getDayOfMonth());
        String yearShort = String.format("%02d", createdAt.getYear() % 100);
        return month + day + "." + yearShort + "." + day;
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
                ", createdAt=" + createdAt +
                ", lastStudyDate=" + lastStudyDate +
                ", averageAccuracy=" + averageAccuracy +
                '}';
    }
}

