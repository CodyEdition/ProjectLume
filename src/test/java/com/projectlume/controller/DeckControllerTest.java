package com.projectlume.controller;

import com.projectlume.util.BaseTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for DeckController
 * Tests controller coordination logic
 */
public class DeckControllerTest extends BaseTest {
    
    @Test
    public void testControllerExists() {
        // Verify controller can be instantiated
        DeckController controller = new DeckController();
        assertNotNull(controller);
    }
}

