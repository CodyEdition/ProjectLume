package com.projectlume.decorator;

public abstract class ServiceDecorator {
    protected final Object wrappedService;
    
    protected ServiceDecorator(Object wrappedService) {
        this.wrappedService = wrappedService;
    }
}

