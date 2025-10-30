package com.projectlume.exception;

/**
 * Exception thrown when a card is not found
 */
public class CardNotFoundException extends ProjectLumeException {
    public CardNotFoundException(Long cardId) {
        super("CARD_NOT_FOUND", "Card not found with ID: " + cardId);
    }
}
