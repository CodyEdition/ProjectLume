package com.projectlume.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Database connection utility class
 */
public class DatabaseConnection {
    private static final Logger logger = Logger.getLogger(DatabaseConnection.class.getName());
    private static Properties properties;
    
    static {
        loadProperties();
    }
    
    /**
     * Load database properties
     */
    private static void loadProperties() {
        properties = new Properties();
        try (InputStream input = DatabaseConnection.class.getClassLoader()
                .getResourceAsStream("database.properties")) {
            if (input == null) {
                logger.severe("Unable to find database.properties file");
                throw new RuntimeException("Database configuration file not found");
            }
            properties.load(input);
        } catch (IOException e) {
            logger.severe("Error loading database properties: " + e.getMessage());
            throw new RuntimeException("Failed to load database configuration", e);
        }
    }
    
    /**
     * Get a database connection
     */
    public static Connection getConnection() throws SQLException {
        try {
            String envUrl = System.getenv("MYSQL_URL");
            String envUsername = System.getenv("MYSQL_USERNAME");
            String envPassword = System.getenv("MYSQL_PASSWORD");
            String envDriver = System.getenv("MYSQL_DRIVER");
            
            String url = envUrl != null && !envUrl.isEmpty() ? envUrl : properties.getProperty("mysql.url");
            String username = envUsername != null ? envUsername : properties.getProperty("mysql.username");
            String password = envPassword != null ? envPassword : properties.getProperty("mysql.password");
            String driver = envDriver != null && !envDriver.isEmpty() ? envDriver : properties.getProperty("mysql.driver");
            
            if (url == null || driver == null) {
                throw new SQLException("MySQL configuration missing: ensure MYSQL_URL/MYSQL_DRIVER env vars or mysql.url/mysql.driver properties are set");
            }
            
            // Load the driver
            Class.forName(driver);
            
            // Create connection
            Connection connection = DriverManager.getConnection(url, username, password);
            logger.fine("Database connection established successfully");
            return connection;
            
        } catch (ClassNotFoundException e) {
            logger.severe("Database driver not found: " + e.getMessage());
            throw new SQLException("Database driver not found", e);
        } catch (SQLException e) {
            logger.severe("Database connection failed: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Close database connection
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
                logger.info("Database connection closed");
            } catch (SQLException e) {
                logger.warning("Error closing database connection: " + e.getMessage());
            }
        }
    }
    
    /**
     * Test database connection
     */
    public static boolean testConnection() {
        try (Connection connection = getConnection()) {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            logger.severe("Database connection test failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Validate database connection
     */
    public static boolean validateConnection() {
        try (Connection connection = getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            String productName = metaData.getDatabaseProductName();
            String productVersion = metaData.getDatabaseProductVersion();
            String driverName = metaData.getDriverName();
            String driverVersion = metaData.getDriverVersion();
            
            logger.info("Database Connection Validation:");
            logger.info("  Product: " + productName + " " + productVersion);
            logger.info("  Driver: " + driverName + " " + driverVersion);
            logger.info("  URL: " + metaData.getURL());
            logger.info("  User: " + metaData.getUserName());
            
            boolean supportsBatchUpdates = metaData.supportsBatchUpdates();
            boolean supportsTransactions = metaData.supportsTransactions();
            boolean supportsStoredProcedures = metaData.supportsStoredProcedures();
            
            logger.info("Database Features:");
            logger.info("  Supports Batch Updates: " + supportsBatchUpdates);
            logger.info("  Supports Transactions: " + supportsTransactions);
            logger.info("  Supports Stored Procedures: " + supportsStoredProcedures);
            
            boolean supportsSelect = metaData.supportsSelectForUpdate();
            logger.info("  Supports SELECT FOR UPDATE: " + supportsSelect);
            
            return true;
        } catch (SQLException e) {
            logger.severe("Database connection validation failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get database type
     */
    public static String getDbType() {
        return "mysql";
    }
}
