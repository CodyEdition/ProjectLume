package com.projectlume.strategy;

import com.projectlume.exception.ValidationException;
import com.projectlume.strategy.impl.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Context class for Validation Strategy pattern
 * Manages validation strategies and provides a unified interface
 */
public class ValidationContext {
    private final Map<String, ValidationStrategy> strategies;
    
    public ValidationContext() {
        this.strategies = new HashMap<>();
        initializeStrategies();
    }
    
    private void initializeStrategies() {
        strategies.put("username", new UsernameValidationStrategy());
        strategies.put("email", new EmailValidationStrategy());
        strategies.put("password", new PasswordValidationStrategy());
        strategies.put("name", new NameValidationStrategy());
        strategies.put("firstName", new NameValidationStrategy());
        strategies.put("lastName", new NameValidationStrategy());
        strategies.put("cardText", new CardTextValidationStrategy());
        strategies.put("frontText", new CardTextValidationStrategy());
        strategies.put("backText", new CardTextValidationStrategy());
        strategies.put("deckName", new DeckNameValidationStrategy());
    }
    
    /**
     * Validate a value using the specified strategy
     * @param strategyKey The key identifying the validation strategy
     * @param value The value to validate
     * @param fieldName The name of the field being validated
     * @throws ValidationException if validation fails
     */
    public void validate(String strategyKey, String value, String fieldName) throws ValidationException {
        ValidationStrategy strategy = strategies.get(strategyKey);
        if (strategy == null) {
            throw new IllegalArgumentException("Unknown validation strategy: " + strategyKey);
        }
        strategy.validate(value, fieldName);
    }
    
    /**
     * Register a custom validation strategy
     * @param key The key to identify the strategy
     * @param strategy The validation strategy implementation
     */
    public void registerStrategy(String key, ValidationStrategy strategy) {
        strategies.put(key, strategy);
    }
}

