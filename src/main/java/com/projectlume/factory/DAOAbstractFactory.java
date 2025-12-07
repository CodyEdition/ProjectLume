package com.projectlume.factory;

import com.projectlume.dao.CardDAO;
import com.projectlume.dao.CardStudyHistoryDAO;
import com.projectlume.dao.DeckDAO;
import com.projectlume.dao.StudySessionDAO;
import com.projectlume.dao.UserDAO;

/**
 * Abstract Factory interface for creating DAO instances
 * Implements Abstract Factory pattern to support multiple database implementations
 */
public interface DAOAbstractFactory {
    /**
     * Create a UserDAO instance
     */
    UserDAO createUserDAO();
    
    /**
     * Create a DeckDAO instance
     */
    DeckDAO createDeckDAO();
    
    /**
     * Create a CardDAO instance
     */
    CardDAO createCardDAO();
    
    /**
     * Create a StudySessionDAO instance
     */
    StudySessionDAO createStudySessionDAO();
    
    /**
     * Create a CardStudyHistoryDAO instance
     */
    CardStudyHistoryDAO createCardStudyHistoryDAO();
}

