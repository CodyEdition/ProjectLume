package com.projectlume.factory.impl;

import com.projectlume.dao.CardDAO;
import com.projectlume.dao.CardStudyHistoryDAO;
import com.projectlume.dao.DeckDAO;
import com.projectlume.dao.StudySessionDAO;
import com.projectlume.dao.UserDAO;
import com.projectlume.factory.DAOAbstractFactory;

/**
 * MySQL implementation of DAO Abstract Factory
 * Creates MySQL-specific DAO instances
 */
public class MySQLDAOFactory implements DAOAbstractFactory {
    
    @Override
    public UserDAO createUserDAO() {
        return new UserDAO();
    }
    
    @Override
    public DeckDAO createDeckDAO() {
        return new DeckDAO();
    }
    
    @Override
    public CardDAO createCardDAO() {
        return new CardDAO();
    }
    
    @Override
    public StudySessionDAO createStudySessionDAO() {
        return new StudySessionDAO();
    }
    
    @Override
    public CardStudyHistoryDAO createCardStudyHistoryDAO() {
        return new CardStudyHistoryDAO();
    }
}

