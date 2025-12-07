package com.projectlume.factory;

import com.projectlume.service.AuthService;
import com.projectlume.service.CardService;
import com.projectlume.service.DeckService;
import com.projectlume.service.StudyService;

/**
 * Factory for creating service instances
 * Centralizes service creation and supports dependency injection
 */
public class ServiceFactory {
    private static ServiceFactory instance;
    private AuthService authService;
    private DeckService deckService;
    private CardService cardService;
    private StudyService studyService;
    
    private ServiceFactory() {
        // Lazy initialization
    }
    
    /**
     * Get singleton instance
     */
    public static synchronized ServiceFactory getInstance() {
        if (instance == null) {
            instance = new ServiceFactory();
        }
        return instance;
    }
    
    /**
     * Create or get AuthService instance
     */
    public AuthService getAuthService() {
        if (authService == null) {
            authService = new AuthService();
        }
        return authService;
    }
    
    /**
     * Create or get DeckService instance
     */
    public DeckService getDeckService() {
        if (deckService == null) {
            deckService = new DeckService();
        }
        return deckService;
    }
    
    /**
     * Create or get CardService instance
     */
    public CardService getCardService() {
        if (cardService == null) {
            cardService = new CardService();
        }
        return cardService;
    }
    
    /**
     * Create or get StudyService instance
     */
    public StudyService getStudyService() {
        if (studyService == null) {
            studyService = new StudyService();
        }
        return studyService;
    }
    
    /**
     * Set custom service instances (for testing or dependency injection)
     */
    public void setAuthService(AuthService service) {
        this.authService = service;
    }
    
    public void setDeckService(DeckService service) {
        this.deckService = service;
    }
    
    public void setCardService(CardService service) {
        this.cardService = service;
    }
    
    public void setStudyService(StudyService service) {
        this.studyService = service;
    }
}

