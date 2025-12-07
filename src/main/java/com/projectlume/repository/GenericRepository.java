package com.projectlume.repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Generic Repository interface
 * Defines common CRUD operations for all entities
 */
public interface GenericRepository<T, ID> {
    /**
     * Create a new entity
     */
    T create(T entity) throws SQLException;
    
    /**
     * Find entity by ID
     */
    T findById(ID id) throws SQLException;
    
    /**
     * Find all entities
     */
    List<T> findAll() throws SQLException;
    
    /**
     * Update an entity
     */
    T update(T entity) throws SQLException;
    
    /**
     * Delete an entity by ID
     */
    boolean delete(ID id) throws SQLException;
}

