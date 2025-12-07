package com.projectlume.decorator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Decorator for adding caching to service methods
 * Implements Decorator pattern to add caching without modifying service classes
 */
public class CachingServiceDecorator {
    private static final Logger logger = Logger.getLogger(CachingServiceDecorator.class.getName());
    private final Object service;
    private final Map<String, CacheEntry> cache;
    private static final long DEFAULT_TTL = 5 * 60 * 1000; // 5 minutes
    
    public CachingServiceDecorator(Object service) {
        this.service = service;
        this.cache = new ConcurrentHashMap<>();
    }
    
    /**
     * Get cached value or compute and cache
     */
    public Object getOrCompute(String key, CacheableOperation operation) {
        CacheEntry entry = cache.get(key);
        
        if (entry != null && !entry.isExpired()) {
            logger.fine("Cache hit for key: " + key);
            return entry.getValue();
        }
        
        logger.fine("Cache miss for key: " + key);
        Object value = operation.compute();
        cache.put(key, new CacheEntry(value, System.currentTimeMillis() + DEFAULT_TTL));
        return value;
    }
    
    /**
     * Invalidate cache entry
     */
    public void invalidate(String key) {
        cache.remove(key);
        logger.fine("Cache invalidated for key: " + key);
    }
    
    /**
     * Clear all cache
     */
    public void clearCache() {
        cache.clear();
        logger.info("Cache cleared");
    }
    
    public Object getService() {
        return service;
    }
    
    /**
     * Functional interface for cacheable operations
     */
    @FunctionalInterface
    public interface CacheableOperation {
        Object compute();
    }
    
    /**
     * Cache entry with expiration
     */
    private static class CacheEntry {
        private final Object value;
        private final long expirationTime;
        
        public CacheEntry(Object value, long expirationTime) {
            this.value = value;
            this.expirationTime = expirationTime;
        }
        
        public Object getValue() {
            return value;
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() > expirationTime;
        }
    }
}

