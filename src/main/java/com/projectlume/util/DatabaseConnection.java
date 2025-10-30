package com.projectlume.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Database connection utility class for Project Lume MVP
 * Handles database connections using JDBC
 */
public class DatabaseConnection {
    private static final Logger logger = Logger.getLogger(DatabaseConnection.class.getName());
    private static Properties properties;
    
    static {
        loadProperties();
    }
    
    /**
     * Load database properties from configuration file
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
     * Get a database connection based on the configured database type
     * @return Connection object
     * @throws SQLException if connection fails
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
     * @param connection Connection to close
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
     * @return true if connection successful, false otherwise
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
     * Get the current database type
     * @return database type (hsqldb or mysql)
     */
    public static String getDbType() {
        return "mysql";
    }
}
