package com.projectlume.observer.impl;

import com.projectlume.observer.Event;
import com.projectlume.model.StudySession;

/**
 * Event fired when a study session is completed
 */
public class StudySessionCompletedEvent extends Event {
    private final StudySession studySession;
    private final Long userId;
    private final Long deckId;
    
    public StudySessionCompletedEvent(StudySession studySession, Long userId, Long deckId) {
        super("STUDY_SESSION_COMPLETED");
        this.studySession = studySession;
        this.userId = userId;
        this.deckId = deckId;
    }
    
    public StudySession getStudySession() {
        return studySession;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public Long getDeckId() {
        return deckId;
    }
}

