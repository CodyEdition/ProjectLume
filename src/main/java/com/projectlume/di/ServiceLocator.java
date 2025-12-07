package com.projectlume.di;

import com.projectlume.service.AuthService;
import com.projectlume.service.CardService;
import com.projectlume.service.DeckService;
import com.projectlume.service.StudyService;

/**
 * Service Locator pattern implementation
 * Provides centralized access to services
 */
public class ServiceLocator {
    private static final ServiceContainer container = ServiceContainer.getInstance();
    
    static {
        // Initialize services
        initializeServices();
    }
    
    private static void initializeServices() {
        container.registerSingleton(AuthService.class, new AuthService());
        container.registerSingleton(DeckService.class, new DeckService());
        container.registerSingleton(CardService.class, new CardService());
        container.registerSingleton(StudyService.class, new StudyService());
    }
    
    /**
     * Get AuthService instance
     */
    public static AuthService getAuthService() {
        return container.get(AuthService.class);
    }
    
    /**
     * Get DeckService instance
     */
    public static DeckService getDeckService() {
        return container.get(DeckService.class);
    }
    
    /**
     * Get CardService instance
     */
    public static CardService getCardService() {
        return container.get(CardService.class);
    }
    
    /**
     * Get StudyService instance
     */
    public static StudyService getStudyService() {
        return container.get(StudyService.class);
    }
    
    /**
     * Get service by class (generic method)
     */
    public static <T> T getService(Class<T> clazz) {
        return container.get(clazz);
    }
}

