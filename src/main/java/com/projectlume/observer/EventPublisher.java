package com.projectlume.observer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

public class EventPublisher {
    private static final Logger logger = Logger.getLogger(EventPublisher.class.getName());
    private final List<EventListener> listeners;
    private static EventPublisher instance;
    
    private EventPublisher() {
        this.listeners = new CopyOnWriteArrayList<>();
    }
    
    public static synchronized EventPublisher getInstance() {
        if (instance == null) {
            instance = new EventPublisher();
        }
        return instance;
    }
    
    public void subscribe(EventListener listener) {
        listeners.add(listener);
        logger.info("Event listener subscribed: " + listener.getClass().getSimpleName());
    }
    
    public void unsubscribe(EventListener listener) {
        listeners.remove(listener);
        logger.info("Event listener unsubscribed: " + listener.getClass().getSimpleName());
    }
    
    public void publish(Event event) {
        logger.fine("Publishing event: " + event.getEventType());
        for (EventListener listener : listeners) {
            try {
                String[] supportedTypes = listener.getSupportedEventTypes();
                for (String type : supportedTypes) {
                    if (type.equals(event.getEventType()) || type.equals("*")) {
                        listener.onEvent(event);
                        break;
                    }
                }
            } catch (Exception e) {
                logger.severe("Error notifying listener " + listener.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
    }
}

