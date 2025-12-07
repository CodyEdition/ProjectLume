package com.projectlume.decorator;

import java.lang.reflect.Method;
import java.util.logging.Logger;

public class LoggingServiceDecorator {
    private static final Logger logger = Logger.getLogger(LoggingServiceDecorator.class.getName());
    private final Object service;
    
    public LoggingServiceDecorator(Object service) {
        this.service = service;
    }
    
    public Object invoke(Method method, Object[] args) throws Exception {
        String methodName = method.getName();
        String className = service.getClass().getSimpleName();
        
        logger.info("Entering " + className + "." + methodName);
        
        try {
            Object result = method.invoke(service, args);
            logger.info("Exiting " + className + "." + methodName + " - Success");
            return result;
        } catch (Exception e) {
            logger.severe("Error in " + className + "." + methodName + ": " + e.getMessage());
            throw e;
        }
    }
    
    public Object getService() {
        return service;
    }
}

