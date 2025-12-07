package com.projectlume.observer;

import java.time.LocalDateTime;

/**
 * Base event class for Observer pattern
 * Represents an event that occurred in the system
 */
public abstract class Event {
    private final LocalDateTime timestamp;
    private final String eventType;
    
    protected Event(String eventType) {
        this.eventType = eventType;
        this.timestamp = LocalDateTime.now();
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public String getEventType() {
        return eventType;
    }
}

