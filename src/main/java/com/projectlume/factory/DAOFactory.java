package com.projectlume.factory;

import com.projectlume.dao.CardDAO;
import com.projectlume.dao.CardStudyHistoryDAO;
import com.projectlume.dao.DeckDAO;
import com.projectlume.dao.StudySessionDAO;
import com.projectlume.dao.UserDAO;
import com.projectlume.factory.impl.MySQLDAOFactory;
import com.projectlume.util.DatabaseConnection;

public class DAOFactory {
    private static DAOAbstractFactory factory;
    
    static {
        String dbType = DatabaseConnection.getDbType();
        if ("mysql".equalsIgnoreCase(dbType)) {
            factory = new MySQLDAOFactory();
        } else {
            factory = new MySQLDAOFactory();
        }
    }
    
    public static UserDAO createUserDAO() {
        return factory.createUserDAO();
    }
    
    public static DeckDAO createDeckDAO() {
        return factory.createDeckDAO();
    }
    
    public static CardDAO createCardDAO() {
        return factory.createCardDAO();
    }
    
    public static StudySessionDAO createStudySessionDAO() {
        return factory.createStudySessionDAO();
    }
    
    public static CardStudyHistoryDAO createCardStudyHistoryDAO() {
        return factory.createCardStudyHistoryDAO();
    }
    
    public static void setFactory(DAOAbstractFactory customFactory) {
        factory = customFactory;
    }
}
