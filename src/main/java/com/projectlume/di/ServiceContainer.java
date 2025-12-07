package com.projectlume.di;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Simple Dependency Injection container
 * Manages service instances and their dependencies
 */
public class ServiceContainer {
    private static final Logger logger = Logger.getLogger(ServiceContainer.class.getName());
    private static final ServiceContainer instance = new ServiceContainer();
    private final Map<Class<?>, Object> singletons = new ConcurrentHashMap<>();
    private final Map<Class<?>, Supplier<?>> factories = new ConcurrentHashMap<>();
    
    private ServiceContainer() {
        // Private constructor for singleton
    }
    
    public static ServiceContainer getInstance() {
        return instance;
    }
    
    /**
     * Register a singleton instance
     */
    public <T> void registerSingleton(Class<T> clazz, T instance) {
        singletons.put(clazz, instance);
        logger.info("Registered singleton: " + clazz.getSimpleName());
    }
    
    /**
     * Register a factory for creating instances
     */
    public <T> void registerFactory(Class<T> clazz, Supplier<T> factory) {
        factories.put(clazz, factory);
        logger.info("Registered factory: " + clazz.getSimpleName());
    }
    
    /**
     * Get an instance of the specified class
     */
    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> clazz) {
        // Check singleton cache first
        Object singleton = singletons.get(clazz);
        if (singleton != null) {
            return (T) singleton;
        }
        
        // Check factory
        Supplier<?> factory = factories.get(clazz);
        if (factory != null) {
            T instance = (T) factory.get();
            // Cache as singleton if not already cached
            if (!singletons.containsKey(clazz)) {
                singletons.put(clazz, instance);
            }
            return instance;
        }
        
        // Try to instantiate using default constructor
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            singletons.put(clazz, instance);
            logger.info("Auto-registered instance: " + clazz.getSimpleName());
            return instance;
        } catch (Exception e) {
            logger.severe("Failed to create instance of " + clazz.getSimpleName() + ": " + e.getMessage());
            throw new RuntimeException("Failed to resolve dependency: " + clazz.getSimpleName(), e);
        }
    }
    
    /**
     * Clear all registered services (useful for testing)
     */
    public void clear() {
        singletons.clear();
        factories.clear();
        logger.info("Service container cleared");
    }
}

