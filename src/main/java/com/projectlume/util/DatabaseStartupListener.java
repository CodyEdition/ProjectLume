package com.projectlume.util;

import com.projectlume.observer.EventPublisher;
import com.projectlume.observer.impl.StudySessionCompletedListener;
import com.projectlume.observer.impl.UserRegisteredListener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.logging.Logger;

@WebListener
public class DatabaseStartupListener implements ServletContextListener {
    private static final Logger logger = Logger.getLogger(DatabaseStartupListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("Initializing database schema and seed data (MySQL)...");
        DatabaseInitializer.initializeDatabase();
        
        // Initialize Observer pattern listeners
        logger.info("Initializing event listeners...");
        EventPublisher eventPublisher = EventPublisher.getInstance();
        eventPublisher.subscribe(new UserRegisteredListener());
        eventPublisher.subscribe(new StudySessionCompletedListener());
        logger.info("Event listeners initialized");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Clean up MySQL drivers and cleanup thread
        cleanupMySQLResources();
    }

    private void cleanupMySQLResources() {
        try {
            Class<?> cleanupThreadClass = Class.forName("com.mysql.cj.jdbc.AbandonedConnectionCleanupThread");
            java.lang.reflect.Method shutdownMethod = cleanupThreadClass.getMethod("checkedShutdown");
            shutdownMethod.invoke(null);
            logger.info("Shut down MySQL abandoned connection cleanup thread");
        } catch (Exception e) {
            logger.fine("MySQL cleanup thread shutdown: " + e.getMessage());
        }
        
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            if (driver.getClass().getName().contains("mysql")) {
                try {
                    DriverManager.deregisterDriver(driver);
                    logger.info("Deregistered MySQL driver: " + driver.getClass().getName());
                } catch (SQLException e) {
                    logger.warning("Failed to deregister MySQL driver: " + e.getMessage());
                }
            }
        }
    }
}


