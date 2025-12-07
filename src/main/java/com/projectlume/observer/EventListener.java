package com.projectlume.observer;

/**
 * Event listener interface for Observer pattern
 * Implementations handle specific types of events
 */
public interface EventListener {
    /**
     * Handle an event
     * @param event The event that occurred
     */
    void onEvent(Event event);
    
    /**
     * Get the event types this listener is interested in
     * @return Array of event type strings
     */
    String[] getSupportedEventTypes();
}

