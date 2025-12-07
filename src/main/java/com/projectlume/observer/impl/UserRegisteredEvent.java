package com.projectlume.observer.impl;

import com.projectlume.observer.Event;
import com.projectlume.model.User;

/**
 * Event fired when a user registers
 */
public class UserRegisteredEvent extends Event {
    private final User user;
    
    public UserRegisteredEvent(User user) {
        super("USER_REGISTERED");
        this.user = user;
    }
    
    public User getUser() {
        return user;
    }
}

