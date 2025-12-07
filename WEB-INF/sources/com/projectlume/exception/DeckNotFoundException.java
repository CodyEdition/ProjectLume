package com.projectlume.exception;

/**
 * Exception thrown when a deck is not found
 */
public class DeckNotFoundException extends ProjectLumeException {
    public DeckNotFoundException(Long deckId) {
        super("DECK_NOT_FOUND", "Deck not found with ID: " + deckId);
    }
}
