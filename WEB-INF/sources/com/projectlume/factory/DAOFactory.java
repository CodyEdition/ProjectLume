package com.projectlume.factory;

import com.projectlume.dao.CardDAO;
import com.projectlume.dao.CardStudyHistoryDAO;
import com.projectlume.dao.DeckDAO;
import com.projectlume.dao.StudySessionDAO;
import com.projectlume.dao.UserDAO;

/**
 * Factory class for creating DAO instances
 * Implements the Factory Design Pattern to centralize object creation
 */
public class DAOFactory {
    
    /**
     * Creates a new UserDAO instance
     * @return UserDAO instance
     */
    public static UserDAO createUserDAO() {
        return new UserDAO();
    }
    
    /**
     * Creates a new DeckDAO instance
     * @return DeckDAO instance
     */
    public static DeckDAO createDeckDAO() {
        return new DeckDAO();
    }
    
    /**
     * Creates a new CardDAO instance
     * @return CardDAO instance
     */
    public static CardDAO createCardDAO() {
        return new CardDAO();
    }
    
    /**
     * Creates a new StudySessionDAO instance
     * @return StudySessionDAO instance
     */
    public static StudySessionDAO createStudySessionDAO() {
        return new StudySessionDAO();
    }
    
    /**
     * Creates a new CardStudyHistoryDAO instance
     * @return CardStudyHistoryDAO instance
     */
    public static CardStudyHistoryDAO createCardStudyHistoryDAO() {
        return new CardStudyHistoryDAO();
    }
}
