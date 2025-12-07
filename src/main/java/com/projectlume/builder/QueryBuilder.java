package com.projectlume.builder;

import java.util.ArrayList;
import java.util.List;

/**
 * Query Builder implementing Builder pattern
 * Builds SQL queries dynamically with support for different database dialects
 */
public class QueryBuilder {
    private final StringBuilder query;
    private final List<Object> parameters;
    private boolean hasWhere;
    
    public QueryBuilder() {
        this.query = new StringBuilder();
        this.parameters = new ArrayList<>();
        this.hasWhere = false;
    }
    
    /**
     * Start building a SELECT query
     */
    public QueryBuilder select(String columns) {
        query.append("SELECT ").append(columns);
        return this;
    }
    
    /**
     * Add FROM clause
     */
    public QueryBuilder from(String table) {
        query.append(" FROM ").append(table);
        return this;
    }
    
    /**
     * Add WHERE clause (automatically handles AND for multiple conditions)
     */
    public QueryBuilder where(String condition, Object value) {
        if (!hasWhere) {
            query.append(" WHERE ");
            hasWhere = true;
        } else {
            query.append(" AND ");
        }
        query.append(condition);
        parameters.add(value);
        return this;
    }
    
    /**
     * Add WHERE clause with custom operator
     */
    public QueryBuilder where(String column, String operator, Object value) {
        if (!hasWhere) {
            query.append(" WHERE ");
            hasWhere = true;
        } else {
            query.append(" AND ");
        }
        query.append(column).append(" ").append(operator).append(" ?");
        parameters.add(value);
        return this;
    }
    
    /**
     * Add ORDER BY clause
     */
    public QueryBuilder orderBy(String column, String direction) {
        query.append(" ORDER BY ").append(column).append(" ").append(direction);
        return this;
    }
    
    /**
     * Add ORDER BY clause (defaults to ASC)
     */
    public QueryBuilder orderBy(String column) {
        return orderBy(column, "ASC");
    }
    
    /**
     * Add LIMIT clause
     */
    public QueryBuilder limit(int count) {
        query.append(" LIMIT ").append(count);
        return this;
    }
    
    /**
     * Add OFFSET clause
     */
    public QueryBuilder offset(int count) {
        query.append(" OFFSET ").append(count);
        return this;
    }
    
    /**
     * Add JOIN clause
     */
    public QueryBuilder join(String table, String condition) {
        query.append(" JOIN ").append(table).append(" ON ").append(condition);
        return this;
    }
    
    /**
     * Add LEFT JOIN clause
     */
    public QueryBuilder leftJoin(String table, String condition) {
        query.append(" LEFT JOIN ").append(table).append(" ON ").append(condition);
        return this;
    }
    
    /**
     * Build the final SQL query string
     */
    public String build() {
        return query.toString();
    }
    
    /**
     * Get the parameters for the query
     */
    public Object[] getParameters() {
        return parameters.toArray();
    }
    
    /**
     * Create a new QueryBuilder instance
     */
    public static QueryBuilder create() {
        return new QueryBuilder();
    }
}

