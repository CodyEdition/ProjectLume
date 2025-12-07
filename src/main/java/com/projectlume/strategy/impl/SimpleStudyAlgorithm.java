package com.projectlume.strategy.impl;

import com.projectlume.model.Card;
import com.projectlume.strategy.StudyAlgorithm;

import java.util.List;

/**
 * Simple study algorithm - sequential card selection
 * Cards are studied in order from first to last
 */
public class SimpleStudyAlgorithm implements StudyAlgorithm {
    
    @Override
    public Card selectNextCard(List<Card> cards, int currentIndex) {
        if (cards == null || cards.isEmpty()) {
            return null;
        }
        if (currentIndex < 0 || currentIndex >= cards.size()) {
            return null;
        }
        return cards.get(currentIndex);
    }
    
    @Override
    public String getAlgorithmName() {
        return "Simple Sequential";
    }
}

