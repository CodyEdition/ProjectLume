package com.projectlume.dto;

/**
 * Data Transfer Object for dashboard-specific deck statistics
 * General (DeckStatsDTO), date code, and formatted lastStudyDate
 */
public class DashDeckStatsDTO {
    private final DeckStatsDTO deckStats;
    private final String dateCode;
    private final String lastStudyDate;

    public DashDeckStatsDTO(DeckStatsDTO deckStats, String dateCode, String lastStudyDate) {
        this.deckStats = deckStats;
        this.dateCode = dateCode;
        this.lastStudyDate = lastStudyDate;
    }

    public DeckStatsDTO getDeckStats() {
        return deckStats;
    }

    public String getDateCode() {
        return dateCode;
    }

    public String getLastStudyDate() {
        return lastStudyDate;
    }

    @Override
    public String toString() {
        return "DashDeckStatsDTO{" +
                "deckStats=" + deckStats.toString() +
                ", dateCode=" + dateCode +
                ", lastStudyDate='" + lastStudyDate + '\'' +
                '}';
    }
}
