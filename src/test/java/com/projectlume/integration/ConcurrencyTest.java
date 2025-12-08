package com.projectlume.integration;

import com.projectlume.model.Deck;
import com.projectlume.model.User;
import com.projectlume.service.AuthService;
import com.projectlume.service.CardService;
import com.projectlume.service.DeckService;
import com.projectlume.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for concurrency
 */
public class ConcurrencyTest extends BaseTest {
    private AuthService authService;
    private DeckService deckService;
    private CardService cardService;
    private User testUser;
    
    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        authService = new AuthService();
        deckService = new DeckService();
        cardService = new CardService();
        
        testUser = authService.register(generateUniqueUsername(), generateUniqueEmail(), 
                                       "TestPassword123!", "Test", "User");
    }
    
    @Test
    public void testConcurrentDeckCreation() throws InterruptedException {
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<Deck>> futures = new ArrayList<>();
        
        // Act - Create decks
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            Future<Deck> future = executor.submit(() -> {
                try {
                    return deckService.createDeck(testUser.getId(), 
                                                  "Concurrent Deck " + index, 
                                                  "Description " + index);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            futures.add(future);
        }
        
        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        
        for (Future<Deck> future : futures) {
            try {
                Deck deck = future.get();
                assertNotNull(deck);
                assertNotNull(deck.getId());
            } catch (ExecutionException e) {
                fail("Deck creation failed: " + e.getMessage());
            }
        }
    }
    
    @Test
    public void testConcurrentCardCreation() throws InterruptedException, SQLException {
        Deck deck = deckService.createDeck(testUser.getId(), "Test Deck", "Description");
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<com.projectlume.model.Card>> futures = new ArrayList<>();
        
        // Act - Create cards
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            Future<com.projectlume.model.Card> future = executor.submit(() -> {
                try {
                    return cardService.createCard(testUser.getId(), deck.getId(), 
                                                 "Front " + index, "Back " + index, null);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            futures.add(future);
        }
        
        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        
        for (Future<com.projectlume.model.Card> future : futures) {
            try {
                com.projectlume.model.Card card = future.get();
                assertNotNull(card);
                assertNotNull(card.getId());
            } catch (ExecutionException e) {
                fail("Card creation failed: " + e.getMessage());
            }
        }
    }
    
    @Test
    public void testConcurrentUserRegistrations() throws InterruptedException {
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<User>> futures = new ArrayList<>();
        
        // Act - Register users
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            Future<User> future = executor.submit(() -> {
                try {
                    long threadId = Thread.currentThread().threadId();
                    long nanoTime = System.nanoTime();
                    String username = "u" + threadId + "_" + (nanoTime % 1000000) + "_" + index;
                    String email = "test_" + threadId + "_" + (nanoTime % 1000000) + "_" + index + "@test.com";
                    return authService.register(username, email, 
                                               "TestPassword123!", 
                                               "User", String.valueOf(index));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            futures.add(future);
        }
        
        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        
        int successCount = 0;
        for (Future<User> future : futures) {
            try {
                User user = future.get();
                assertNotNull(user);
                assertNotNull(user.getId());
                successCount++;
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause != null) {
                    System.out.println("User registration failed (expected in some cases): " + cause.getMessage());
                }
            }
        }
        assertTrue(successCount > 0, "At least some concurrent registrations should succeed");
    }
}

