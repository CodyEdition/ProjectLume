package com.projectlume.observer.impl;

import com.projectlume.observer.Event;
import com.projectlume.observer.EventListener;

import java.util.logging.Logger;

/**
 * Listener for study session completed events
 */
public class StudySessionCompletedListener implements EventListener {
    private static final Logger logger = Logger.getLogger(StudySessionCompletedListener.class.getName());
    
    @Override
    public void onEvent(Event event) {
        if (event instanceof StudySessionCompletedEvent) {
            StudySessionCompletedEvent studyEvent = (StudySessionCompletedEvent) event;
            handleStudySessionCompleted(studyEvent);
        }
    }
    
    private void handleStudySessionCompleted(StudySessionCompletedEvent event) {
        logger.info("Study session completed - User: " + event.getUserId() + 
                   ", Deck: " + event.getDeckId() + 
                   ", Cards Studied: " + event.getStudySession().getCardsStudied() +
                   ", Accuracy: " + calculateAccuracy(event.getStudySession()) + "%");
        
        // Future: Could trigger notifications, analytics, achievements, etc.
    }
    
    private double calculateAccuracy(com.projectlume.model.StudySession session) {
        int total = session.getCorrectAnswers() + session.getIncorrectAnswers();
        if (total == 0) {
            return 0.0;
        }
        return (double) session.getCorrectAnswers() / total * 100.0;
    }
    
    @Override
    public String[] getSupportedEventTypes() {
        return new String[]{"STUDY_SESSION_COMPLETED"};
    }
}

