package com.projectlume.util;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.logging.Logger;

@WebListener
public class DatabaseStartupListener implements ServletContextListener {
    private static final Logger logger = Logger.getLogger(DatabaseStartupListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("Initializing database schema and seed data (MySQL)...");
        DatabaseInitializer.initializeDatabase();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // no-op
    }
}


