package com.projectlume.observer.impl;

import com.projectlume.observer.Event;
import com.projectlume.observer.EventListener;

import java.util.logging.Logger;

/**
 * Listener for user registration events
 */
public class UserRegisteredListener implements EventListener {
    private static final Logger logger = Logger.getLogger(UserRegisteredListener.class.getName());
    
    @Override
    public void onEvent(Event event) {
        if (event instanceof UserRegisteredEvent) {
            UserRegisteredEvent userEvent = (UserRegisteredEvent) event;
            handleUserRegistered(userEvent);
        }
    }
    
    private void handleUserRegistered(UserRegisteredEvent event) {
        logger.info("New user registered - Username: " + event.getUser().getUsername() + 
                   ", Email: " + event.getUser().getEmail());
        
        // Future: Could trigger welcome email, analytics, onboarding flow, etc.
    }
    
    @Override
    public String[] getSupportedEventTypes() {
        return new String[]{"USER_REGISTERED"};
    }
}

