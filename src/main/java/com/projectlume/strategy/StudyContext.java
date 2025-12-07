package com.projectlume.strategy;

import com.projectlume.model.Card;

import java.util.List;

/**
 * Context class for Study Algorithm Strategy pattern
 * Manages study algorithms and provides a unified interface
 */
public class StudyContext {
    private StudyAlgorithm algorithm;
    
    public StudyContext(StudyAlgorithm algorithm) {
        this.algorithm = algorithm;
    }
    
    /**
     * Set the study algorithm to use
     * @param algorithm The study algorithm strategy
     */
    public void setAlgorithm(StudyAlgorithm algorithm) {
        this.algorithm = algorithm;
    }
    
    /**
     * Select the next card using the current algorithm
     * @param cards List of available cards
     * @param currentIndex Current index in the study session
     * @return The selected card
     */
    public Card selectNextCard(List<Card> cards, int currentIndex) {
        if (algorithm == null) {
            throw new IllegalStateException("Study algorithm not set");
        }
        return algorithm.selectNextCard(cards, currentIndex);
    }
    
    /**
     * Get the name of the current algorithm
     * @return Algorithm name
     */
    public String getAlgorithmName() {
        return algorithm != null ? algorithm.getAlgorithmName() : "None";
    }
}

