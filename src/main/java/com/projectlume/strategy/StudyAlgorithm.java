package com.projectlume.strategy;

import com.projectlume.model.Card;

import java.util.List;

/**
 * Strategy interface for study algorithms
 * Implements Strategy pattern to allow different card selection algorithms
 */
public interface StudyAlgorithm {
    /**
     * Select the next card to study from the available cards
     * @param cards List of available cards
     * @param currentIndex Current index in the study session
     * @return The selected card to study next
     */
    Card selectNextCard(List<Card> cards, int currentIndex);
    
    /**
     * Get the name of the algorithm
     * @return Algorithm name
     */
    String getAlgorithmName();
}

