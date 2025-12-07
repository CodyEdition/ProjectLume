package com.projectlume.strategy.impl;

import com.projectlume.model.Card;
import com.projectlume.strategy.StudyAlgorithm;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Spaced repetition algorithm - prioritizes difficult cards
 * Cards with higher difficulty are shown more frequently
 */
public class SpacedRepetitionAlgorithm implements StudyAlgorithm {
    
    @Override
    public Card selectNextCard(List<Card> cards, int currentIndex) {
        if (cards == null || cards.isEmpty()) {
            return null;
        }
        
        // If we've gone through all cards, start prioritizing difficult ones
        if (currentIndex >= cards.size()) {
            // Find cards with HARD difficulty
            List<Card> hardCards = cards.stream()
                    .filter(card -> card.getDifficultyLevel() == Card.DifficultyLevel.HARD)
                    .collect(Collectors.toList());
            
            if (!hardCards.isEmpty()) {
                // Return a hard card (could be randomized or sequential)
                int hardIndex = (currentIndex - cards.size()) % hardCards.size();
                return hardCards.get(hardIndex);
            }
            
            // If no hard cards, find MEDIUM difficulty cards
            List<Card> mediumCards = cards.stream()
                    .filter(card -> card.getDifficultyLevel() == Card.DifficultyLevel.MEDIUM)
                    .collect(Collectors.toList());
            
            if (!mediumCards.isEmpty()) {
                int mediumIndex = (currentIndex - cards.size()) % mediumCards.size();
                return mediumCards.get(mediumIndex);
            }
        }
        
        // Normal sequential selection for first pass
        if (currentIndex < cards.size()) {
            return cards.get(currentIndex);
        }
        
        return null;
    }
    
    @Override
    public String getAlgorithmName() {
        return "Spaced Repetition";
    }
}

