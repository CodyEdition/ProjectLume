package com.projectlume.controller;

import com.projectlume.dto.DeckStatsDTO;
import com.projectlume.service.DeckService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import com.projectlume.util.ThreadPoolManager;

/**
 * Controller for managing dashboard operations.
 * 
 * Demonstrates multi-threading by fetching deck statistics in parallel.
 */
public class DashboardController extends BaseController {
    private static final Logger logger = Logger.getLogger(DashboardController.class.getName());
    private final DeckService deckService;
    
    public DashboardController() {
        this.deckService = new DeckService();
    }
    
    /**
     * Get dashboard data with deck statistics
     * Uses multi-threading to fetch statistics in parallel for better performance
     */
    public void getDashboardData(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        try {
            Long userId = getCurrentUserId(request);
            if (userId == null) {
                requireAuthentication(request, response);
                return;
            }
            
            // Controller calls Service (domain layer)
            List<DeckStatsDTO> deckStats = getDeckStatisticsParallel(userId);
            
            // Set attributes for presentation layer
            setUserAttribute(request);
            request.setAttribute("deckStats", deckStats);
            request.setAttribute("currentPage", "dashboard");
            
        } catch (SQLException e) {
            logger.severe("Error loading dashboard: " + e.getMessage());
            request.setAttribute("error", "Failed to load dashboard statistics.");
            request.setAttribute("currentPage", "dashboard");
        } catch (InterruptedException | ExecutionException e) {
            logger.severe("Error in parallel processing: " + e.getMessage());
            request.setAttribute("error", "Failed to load dashboard statistics.");
            request.setAttribute("currentPage", "dashboard");
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Get deck statistics using ExecutorService
     */
    private List<DeckStatsDTO> getDeckStatisticsParallel(Long userId) 
            throws SQLException, InterruptedException, ExecutionException {
        ExecutorService executor = ThreadPoolManager.getFixedThreadPool();
        
        try {
            // Controller calls Service (domain layer)
            Future<List<DeckStatsDTO>> future = executor.submit(new Callable<List<DeckStatsDTO>>() {
                @Override
                public List<DeckStatsDTO> call() throws SQLException {
                    logger.info("Fetching deck statistics in thread: " + Thread.currentThread().getName());
                    return deckService.getDeckStatisticsForUser(userId);
                }
            });
            
            // Wait for the result
            List<DeckStatsDTO> result = future.get();
            logger.info("Deck statistics fetched successfully using multi-threading");
            return result;
            
        } finally {
        }
    }
    
    /**
     * Get deck statistics synchronously (fallback method)
     */
    public List<DeckStatsDTO> getDeckStatisticsForUser(Long userId) throws SQLException {
        return deckService.getDeckStatisticsForUser(userId);
    }
}

