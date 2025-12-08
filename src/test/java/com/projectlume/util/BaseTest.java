package com.projectlume.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Base test class
 */
public abstract class BaseTest {
    protected static final Logger logger = Logger.getLogger(BaseTest.class.getName());
    private static boolean loggingConfigured = false;
    
    static {
        if (!loggingConfigured) {
            configureTestLogging();
            loggingConfigured = true;
        }
    }
    
    private static void configureTestLogging() {
        try {
            InputStream loggingConfig = BaseTest.class.getClassLoader()
                    .getResourceAsStream("logging.properties");
            if (loggingConfig != null) {
                LogManager.getLogManager().readConfiguration(loggingConfig);
                loggingConfig.close();
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not load test logging.properties: " + e.getMessage());
        }
    }
    
    @BeforeEach
    public void setUp() throws Exception {
        TestDatabaseSetup.initializeTestDatabase();
        logger.fine("Test setup completed for: " + getClass().getSimpleName());
    }
    
    @AfterEach
    public void tearDown() throws SQLException {
        TestDatabaseSetup.cleanupTestData();
        logger.fine("Test teardown completed for: " + getClass().getSimpleName());
    }
    
    protected String generateUniqueUsername() {
        return "testuser_" + System.currentTimeMillis() + "_" + System.nanoTime();
    }
    
    protected String generateUniqueEmail() {
        return "test_" + System.currentTimeMillis() + "_" + System.nanoTime() + "@test.com";
    }
}

