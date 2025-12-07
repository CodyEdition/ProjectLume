package com.projectlume.service;

import java.sql.SQLException;
import java.util.logging.Logger;

public abstract class BaseService {
    protected final Logger logger;
    
    protected BaseService() {
        this.logger = Logger.getLogger(this.getClass().getName());
    }
    
    protected <T> T executeCreate(CreateOperation<T> operation) throws SQLException {
        try {
            logger.info("Starting create operation in " + getClass().getSimpleName());
            T result = operation.execute();
            logger.info("Create operation completed successfully in " + getClass().getSimpleName());
            return result;
        } catch (SQLException e) {
            logger.severe("Create operation failed in " + getClass().getSimpleName() + ": " + e.getMessage());
            throw e;
        }
    }
    
    protected <T> T executeUpdate(UpdateOperation<T> operation) throws SQLException {
        try {
            logger.info("Starting update operation in " + getClass().getSimpleName());
            T result = operation.execute();
            logger.info("Update operation completed successfully in " + getClass().getSimpleName());
            return result;
        } catch (SQLException e) {
            logger.severe("Update operation failed in " + getClass().getSimpleName() + ": " + e.getMessage());
            throw e;
        }
    }
    
    protected boolean executeDelete(DeleteOperation operation) throws SQLException {
        try {
            logger.info("Starting delete operation in " + getClass().getSimpleName());
            boolean result = operation.execute();
            logger.info("Delete operation completed successfully in " + getClass().getSimpleName());
            return result;
        } catch (SQLException e) {
            logger.severe("Delete operation failed in " + getClass().getSimpleName() + ": " + e.getMessage());
            throw e;
        }
    }
    
    protected <T> T executeRead(ReadOperation<T> operation) throws SQLException {
        try {
            logger.fine("Executing read operation in " + getClass().getSimpleName());
            return operation.execute();
        } catch (SQLException e) {
            logger.severe("Read operation failed in " + getClass().getSimpleName() + ": " + e.getMessage());
            throw e;
        }
    }
    
    @FunctionalInterface
    protected interface CreateOperation<T> {
        T execute() throws SQLException;
    }
    
    @FunctionalInterface
    protected interface UpdateOperation<T> {
        T execute() throws SQLException;
    }
    
    @FunctionalInterface
    protected interface DeleteOperation {
        boolean execute() throws SQLException;
    }
    
    @FunctionalInterface
    protected interface ReadOperation<T> {
        T execute() throws SQLException;
    }
}

