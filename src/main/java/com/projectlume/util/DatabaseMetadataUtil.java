package com.projectlume.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Utility class for querying database metadata using JDBC DatabaseMetaData.
 */
public class DatabaseMetadataUtil {
    private static final Logger logger = Logger.getLogger(DatabaseMetadataUtil.class.getName());
    
    /**
     * Get database information including product name, version, and driver info
     */
    public static DatabaseInfo getDatabaseInfo() throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            DatabaseInfo info = new DatabaseInfo();
            info.setProductName(metaData.getDatabaseProductName());
            info.setProductVersion(metaData.getDatabaseProductVersion());
            info.setDriverName(metaData.getDriverName());
            info.setDriverVersion(metaData.getDriverVersion());
            info.setDriverMajorVersion(metaData.getDriverMajorVersion());
            info.setDriverMinorVersion(metaData.getDriverMinorVersion());
            info.setUrl(metaData.getURL());
            info.setUserName(metaData.getUserName());
            
            logger.info("Database Info - Product: " + info.getProductName() + 
                       ", Version: " + info.getProductVersion() +
                       ", Driver: " + info.getDriverName());
            
            return info;
        }
    }
    
    /**
     * Check supported database features
     */
    public static SupportedFeatures checkSupportedFeatures() throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            SupportedFeatures features = new SupportedFeatures();
            features.setSupportsBatchUpdates(metaData.supportsBatchUpdates());
            features.setSupportsTransactions(metaData.supportsTransactions());
            features.setSupportsStoredProcedures(metaData.supportsStoredProcedures());
            features.setSupportsResultSetTypeScrollInsensitive(metaData.supportsResultSetType(ResultSet.TYPE_SCROLL_INSENSITIVE));
            features.setSupportsResultSetTypeScrollSensitive(metaData.supportsResultSetType(ResultSet.TYPE_SCROLL_SENSITIVE));
            features.setSupportsResultSetConcurrency(metaData.supportsResultSetConcurrency(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE));
            features.setSupportsSelectForUpdate(metaData.supportsSelectForUpdate());
            features.setSupportsPositionedUpdate(metaData.supportsPositionedUpdate());
            features.setSupportsPositionedDelete(metaData.supportsPositionedDelete());
            
            logger.info("Database Features - Batch Updates: " + features.isSupportsBatchUpdates() +
                       ", Transactions: " + features.isSupportsTransactions() +
                       ", Stored Procedures: " + features.isSupportsStoredProcedures());
            
            return features;
        }
    }
    
    /**
     * Validate that a table exists and has required columns
     */
    public static boolean validateTableStructure(String tableName, String[] requiredColumns) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            try (ResultSet tables = metaData.getTables(null, null, tableName, new String[]{"TABLE"})) {
                if (!tables.next()) {
                    logger.warning("Table " + tableName + " does not exist");
                    return false;
                }
            }
            
            if (requiredColumns != null && requiredColumns.length > 0) {
                List<String> existingColumns = getTableColumns(tableName);
                for (String requiredColumn : requiredColumns) {
                    if (!existingColumns.contains(requiredColumn.toLowerCase())) {
                        logger.warning("Required column " + requiredColumn + " not found in table " + tableName);
                        return false;
                    }
                }
            }
            
            logger.info("Table " + tableName + " structure validated successfully");
            return true;
        }
    }
    
    /**
     * Get all columns for a table
     */
    public static List<String> getTableColumns(String tableName) throws SQLException {
        List<String> columns = new ArrayList<>();
        
        try (Connection connection = DatabaseConnection.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            try (ResultSet columnsResultSet = metaData.getColumns(null, null, tableName, "%")) {
                while (columnsResultSet.next()) {
                    String columnName = columnsResultSet.getString("COLUMN_NAME");
                    String dataType = columnsResultSet.getString("TYPE_NAME");
                    int columnSize = columnsResultSet.getInt("COLUMN_SIZE");
                    boolean nullable = columnsResultSet.getInt("NULLABLE") == DatabaseMetaData.columnNullable;
                    
                    columns.add(columnName.toLowerCase());
                    logger.fine("Column: " + columnName + " (" + dataType + "(" + columnSize + "), nullable: " + nullable + ")");
                }
            }
        }
        
        return columns;
    }
    
    /**
     * Get detailed column information for a table
     */
    public static Map<String, ColumnInfo> getTableColumnDetails(String tableName) throws SQLException {
        Map<String, ColumnInfo> columnMap = new HashMap<>();
        
        try (Connection connection = DatabaseConnection.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            try (ResultSet columnsResultSet = metaData.getColumns(null, null, tableName, "%")) {
                while (columnsResultSet.next()) {
                    ColumnInfo columnInfo = new ColumnInfo();
                    columnInfo.setColumnName(columnsResultSet.getString("COLUMN_NAME"));
                    columnInfo.setDataType(columnsResultSet.getString("TYPE_NAME"));
                    columnInfo.setColumnSize(columnsResultSet.getInt("COLUMN_SIZE"));
                    columnInfo.setNullable(columnsResultSet.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
                    columnInfo.setAutoIncrement("YES".equals(columnsResultSet.getString("IS_AUTOINCREMENT")));
                    columnInfo.setPrimaryKey(columnsResultSet.getString("KEY_SEQ") != null);
                    
                    columnMap.put(columnInfo.getColumnName().toLowerCase(), columnInfo);
                }
            }
        }
        
        return columnMap;
    }
    
    /**
     * Database information holder
     */
    public static class DatabaseInfo {
        private String productName;
        private String productVersion;
        private String driverName;
        private String driverVersion;
        private int driverMajorVersion;
        private int driverMinorVersion;
        private String url;
        private String userName;
        
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public String getProductVersion() { return productVersion; }
        public void setProductVersion(String productVersion) { this.productVersion = productVersion; }
        public String getDriverName() { return driverName; }
        public void setDriverName(String driverName) { this.driverName = driverName; }
        public String getDriverVersion() { return driverVersion; }
        public void setDriverVersion(String driverVersion) { this.driverVersion = driverVersion; }
        public int getDriverMajorVersion() { return driverMajorVersion; }
        public void setDriverMajorVersion(int driverMajorVersion) { this.driverMajorVersion = driverMajorVersion; }
        public int getDriverMinorVersion() { return driverMinorVersion; }
        public void setDriverMinorVersion(int driverMinorVersion) { this.driverMinorVersion = driverMinorVersion; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
    }
    
    /**
     * Supported features holder
     */
    public static class SupportedFeatures {
        private boolean supportsBatchUpdates;
        private boolean supportsTransactions;
        private boolean supportsStoredProcedures;
        private boolean supportsResultSetTypeScrollInsensitive;
        private boolean supportsResultSetTypeScrollSensitive;
        private boolean supportsResultSetConcurrency;
        private boolean supportsSelectForUpdate;
        private boolean supportsPositionedUpdate;
        private boolean supportsPositionedDelete;
        
        // Getters and setters
        public boolean isSupportsBatchUpdates() { return supportsBatchUpdates; }
        public void setSupportsBatchUpdates(boolean supportsBatchUpdates) { this.supportsBatchUpdates = supportsBatchUpdates; }
        public boolean isSupportsTransactions() { return supportsTransactions; }
        public void setSupportsTransactions(boolean supportsTransactions) { this.supportsTransactions = supportsTransactions; }
        public boolean isSupportsStoredProcedures() { return supportsStoredProcedures; }
        public void setSupportsStoredProcedures(boolean supportsStoredProcedures) { this.supportsStoredProcedures = supportsStoredProcedures; }
        public boolean isSupportsResultSetTypeScrollInsensitive() { return supportsResultSetTypeScrollInsensitive; }
        public void setSupportsResultSetTypeScrollInsensitive(boolean supportsResultSetTypeScrollInsensitive) { this.supportsResultSetTypeScrollInsensitive = supportsResultSetTypeScrollInsensitive; }
        public boolean isSupportsResultSetTypeScrollSensitive() { return supportsResultSetTypeScrollSensitive; }
        public void setSupportsResultSetTypeScrollSensitive(boolean supportsResultSetTypeScrollSensitive) { this.supportsResultSetTypeScrollSensitive = supportsResultSetTypeScrollSensitive; }
        public boolean isSupportsResultSetConcurrency() { return supportsResultSetConcurrency; }
        public void setSupportsResultSetConcurrency(boolean supportsResultSetConcurrency) { this.supportsResultSetConcurrency = supportsResultSetConcurrency; }
        public boolean isSupportsSelectForUpdate() { return supportsSelectForUpdate; }
        public void setSupportsSelectForUpdate(boolean supportsSelectForUpdate) { this.supportsSelectForUpdate = supportsSelectForUpdate; }
        public boolean isSupportsPositionedUpdate() { return supportsPositionedUpdate; }
        public void setSupportsPositionedUpdate(boolean supportsPositionedUpdate) { this.supportsPositionedUpdate = supportsPositionedUpdate; }
        public boolean isSupportsPositionedDelete() { return supportsPositionedDelete; }
        public void setSupportsPositionedDelete(boolean supportsPositionedDelete) { this.supportsPositionedDelete = supportsPositionedDelete; }
    }
    
    /**
     * Column information holder
     */
    public static class ColumnInfo {
        private String columnName;
        private String dataType;
        private int columnSize;
        private boolean nullable;
        private boolean autoIncrement;
        private boolean primaryKey;
        
        // Getters and setters
        public String getColumnName() { return columnName; }
        public void setColumnName(String columnName) { this.columnName = columnName; }
        public String getDataType() { return dataType; }
        public void setDataType(String dataType) { this.dataType = dataType; }
        public int getColumnSize() { return columnSize; }
        public void setColumnSize(int columnSize) { this.columnSize = columnSize; }
        public boolean isNullable() { return nullable; }
        public void setNullable(boolean nullable) { this.nullable = nullable; }
        public boolean isAutoIncrement() { return autoIncrement; }
        public void setAutoIncrement(boolean autoIncrement) { this.autoIncrement = autoIncrement; }
        public boolean isPrimaryKey() { return primaryKey; }
        public void setPrimaryKey(boolean primaryKey) { this.primaryKey = primaryKey; }
    }
}

