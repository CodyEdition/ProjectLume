package com.projectlume.repository.impl;

import com.projectlume.repository.GenericRepository;
import com.projectlume.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Generic Repository implementation
 * Provides base CRUD operations using reflection and SQL builders
 * Reduces code duplication in DAOs
 */
public class GenericRepositoryImpl<T, ID> implements GenericRepository<T, ID> {
    private final String tableName;
    private final String idColumn;
    private final Function<ResultSet, T> mapper;
    
    public GenericRepositoryImpl(String tableName, String idColumn, 
                                 Function<ResultSet, T> mapper) {
        this.tableName = tableName;
        this.idColumn = idColumn;
        this.mapper = mapper;
    }
    
    @Override
    public T create(T entity) throws SQLException {
        // This is a template - actual implementations should override with specific SQL
        throw new UnsupportedOperationException("Generic create not implemented. Override in specific repository.");
    }
    
    @Override
    public T findById(ID id) throws SQLException {
        String sql = "SELECT * FROM " + tableName + " WHERE " + idColumn + " = ? AND is_active = true";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapper.apply(resultSet);
                }
            }
        }
        return null;
    }
    
    @Override
    public List<T> findAll() throws SQLException {
        List<T> entities = new ArrayList<>();
        String sql = "SELECT * FROM " + tableName + " WHERE is_active = true";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                entities.add(mapper.apply(resultSet));
            }
        }
        return entities;
    }
    
    @Override
    public T update(T entity) throws SQLException {
        // This is a template - actual implementations should override with specific SQL
        throw new UnsupportedOperationException("Generic update not implemented. Override in specific repository.");
    }
    
    @Override
    public boolean delete(ID id) throws SQLException {
        String sql = "UPDATE " + tableName + " SET is_active = false WHERE " + idColumn + " = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, id);
            return statement.executeUpdate() > 0;
        }
    }
}

