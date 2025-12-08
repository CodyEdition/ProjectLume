package com.projectlume.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;

public class DatabaseInitializer {
    private static final Logger logger = Logger.getLogger(DatabaseInitializer.class.getName());
    
    public static boolean initializeDatabase() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            logger.info("Starting database initialization...");
            
            // Use DatabaseMetaData to check existing tables before initialization
            DatabaseMetaData metaData = connection.getMetaData();
            List<String> existingTables = getExistingTables(metaData);
            logger.info("Existing tables found: " + existingTables.size());
            
            String schemaFile = "schema-mysql.sql";
            String schema = readResourceFile(schemaFile);
            executeSQLScript(connection, schema);
            
            // Validate table structure using DatabaseMetaData after initialization
            validateTablesAfterInit(metaData);
            
            logger.info("Database initialization completed successfully");
            return true;
            
        } catch (SQLException e) {
            logger.severe("Database initialization failed: " + e.getMessage());
            return false;
        } catch (IOException e) {
            logger.severe("Failed to read schema file: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get list of existing tables using DatabaseMetaData.getTables()
     */
    private static List<String> getExistingTables(DatabaseMetaData metaData) throws SQLException {
        List<String> tables = new ArrayList<>();
        try (ResultSet tablesResultSet = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
            while (tablesResultSet.next()) {
                String tableName = tablesResultSet.getString("TABLE_NAME");
                tables.add(tableName);
                logger.fine("Found existing table: " + tableName);
            }
        }
        return tables;
    }
    
    /**
     * Validate table structures using DatabaseMetaData after initialization
     */
    private static void validateTablesAfterInit(DatabaseMetaData metaData) throws SQLException {
        String[] requiredTables = {"users", "decks", "cards", "study_sessions", "card_study_history"};
        
        logger.info("Validating table structures using DatabaseMetaData...");
        try (ResultSet tablesResultSet = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
            List<String> existingTables = new ArrayList<>();
            while (tablesResultSet.next()) {
                String tableName = tablesResultSet.getString("TABLE_NAME");
                existingTables.add(tableName.toLowerCase());
            }
            
            for (String requiredTable : requiredTables) {
                if (existingTables.contains(requiredTable.toLowerCase())) {
                    // Use DatabaseMetaData.getColumns() to validate table structure
                    int columnCount = getColumnCount(metaData, requiredTable);
                    logger.info("Table " + requiredTable + " validated: " + columnCount + " columns found");
                } else {
                    logger.warning("Required table " + requiredTable + " not found after initialization");
                }
            }
        }
    }
    
    /**
     * Get column count for a table using DatabaseMetaData.getColumns()
     */
    private static int getColumnCount(DatabaseMetaData metaData, String tableName) throws SQLException {
        int count = 0;
        try (ResultSet columnsResultSet = metaData.getColumns(null, null, tableName, "%")) {
            while (columnsResultSet.next()) {
                count++;
            }
        }
        return count;
    }
    
    private static String readResourceFile(String filename) throws IOException {
        StringBuilder content = new StringBuilder();
        
        try (InputStream inputStream = DatabaseInitializer.class.getClassLoader()
                .getResourceAsStream(filename)) {
            if (inputStream == null) {
                throw new IOException("Schema resource not found: " + filename);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            }
        }
        
        return content.toString();
    }
    
    private static void executeSQLScript(Connection connection, String sqlScript) throws SQLException {
        String normalized = sqlScript.replace("\r\n", "\n").replace('\r', '\n');
        
        // Process the script line by line to handle DELIMITER statements
        StringBuilder currentStatement = new StringBuilder();
        String currentDelimiter = ";";
        
        try (Statement statement = connection.createStatement()) {
            for (String line : normalized.split("\n")) {
                String trimmed = line.trim();
                
                // Skip empty lines and comments
                if (trimmed.isEmpty() || trimmed.startsWith("--")) {
                    continue;
                }
                
                // Handle DELIMITER commands (MySQL client-specific, not valid SQL)
                if (trimmed.toUpperCase().startsWith("DELIMITER")) {
                    String[] parts = trimmed.split("\\s+");
                    if (parts.length > 1) {
                        currentDelimiter = parts[1];
                    } else {
                        // DELIMITER without argument resets to semicolon
                        currentDelimiter = ";";
                    }
                    continue; // Skip DELIMITER lines - they're not SQL
                }
                
                // Check if this line ends the current statement
                if (trimmed.endsWith(currentDelimiter)) {
                    // Remove the delimiter from the end
                    String lineWithoutDelimiter = trimmed.substring(0, trimmed.length() - currentDelimiter.length()).trim();
                    if (!lineWithoutDelimiter.isEmpty()) {
                        currentStatement.append(lineWithoutDelimiter);
                    }
                    
                    // Execute the complete statement
                    String sql = currentStatement.toString().trim();
                    if (!sql.isEmpty()) {
                        try {
                            statement.execute(sql);
                            logger.info("Executed SQL: " + sql.substring(0, Math.min(sql.length(), 50)) + "...");
                        } catch (SQLException e) {
                            logger.warning("Failed to execute SQL: " + sql + " - " + e.getMessage());
                        }
                    }
                    
                    // Reset for next statement
                    currentStatement.setLength(0);
                    // Note: Delimiter reset happens when we encounter DELIMITER ; command
                } else {
                    // Continue building the current statement
                    currentStatement.append(line).append('\n');
                }
            }
            
            // Execute any remaining statement
            String sql = currentStatement.toString().trim();
            if (!sql.isEmpty()) {
                try {
                    statement.execute(sql);
                    logger.info("Executed SQL: " + sql.substring(0, Math.min(sql.length(), 50)) + "...");
                } catch (SQLException e) {
                    logger.warning("Failed to execute SQL: " + sql + " - " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Deregister MySQL drivers
     */
    private static void deregisterDrivers() {
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
    
    /**
     * Test database connection and initialization
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        System.out.println("Project Lume Database Initializer");
        System.out.println("==================================");
        
        try {
            // Test database connection
            if (DatabaseConnection.testConnection()) {
                System.out.println("✓ Database connection successful");
                
                // Initialize database
                if (initializeDatabase()) {
                    System.out.println("✓ Database initialization completed");
                } else {
                    System.out.println("✗ Database initialization failed");
                }
            } else {
                System.out.println("✗ Database connection failed");
            }
        } finally {
            deregisterDrivers();
            logger.info("Database initializer shutdown complete");
        }
    }
}
