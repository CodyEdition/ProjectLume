package com.projectlume.strategy;

import com.projectlume.exception.ValidationException;

public interface ValidationStrategy {
    void validate(String value, String fieldName) throws ValidationException;
}

