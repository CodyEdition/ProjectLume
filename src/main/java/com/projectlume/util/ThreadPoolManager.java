package com.projectlume.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Thread pool manager for managing ExecutorService instances.
 * 
 * This singleton manages thread pools for parallel data fetching and async background tasks.
 */
public class ThreadPoolManager {
    private static final Logger logger = Logger.getLogger(ThreadPoolManager.class.getName());
    private static final int FIXED_POOL_SIZE = 5;
    
    private static ThreadPoolManager instance;
    private ExecutorService fixedThreadPool;
    private ExecutorService cachedThreadPool;
    
    private ThreadPoolManager() {
        fixedThreadPool = Executors.newFixedThreadPool(FIXED_POOL_SIZE);
        cachedThreadPool = Executors.newCachedThreadPool();
        logger.info("ThreadPoolManager initialized with fixed pool size: " + FIXED_POOL_SIZE);
    }
    
    /**
     * Get singleton instance
     */
    public static synchronized ThreadPoolManager getInstance() {
        if (instance == null) {
            instance = new ThreadPoolManager();
        }
        return instance;
    }
    
    /**
     * Get fixed thread pool for parallel data fetching
     */
    public static ExecutorService getFixedThreadPool() {
        return getInstance().fixedThreadPool;
    }
    
    /**
     * Get cached thread pool for async background tasks
     */
    public static ExecutorService getCachedThreadPool() {
        return getInstance().cachedThreadPool;
    }
    
    /**
     * Shutdown all thread pools gracefully
     */
    public void shutdown() {
        logger.info("Shutting down thread pools...");
        
        shutdownExecutor(fixedThreadPool, "FixedThreadPool");
        shutdownExecutor(cachedThreadPool, "CachedThreadPool");
        
        logger.info("All thread pools shut down successfully");
    }
    
    /**
     * Shutdown an executor service gracefully
     */
    private void shutdownExecutor(ExecutorService executor, String name) {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    logger.warning(name + " did not terminate gracefully, forcing shutdown");
                    executor.shutdownNow();
                    if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                        logger.severe(name + " did not terminate");
                    }
                } else {
                    logger.info(name + " shut down gracefully");
                }
            } catch (InterruptedException e) {
                logger.warning(name + " shutdown interrupted: " + e.getMessage());
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * Check if thread pools are active
     */
    public boolean isActive() {
        return (fixedThreadPool != null && !fixedThreadPool.isShutdown()) ||
               (cachedThreadPool != null && !cachedThreadPool.isShutdown());
    }
}

