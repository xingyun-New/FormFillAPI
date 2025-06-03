package com.formfill.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FormFillAPI Spring Boot Application Main Class
 * 
 * @author FormFillAPI
 * @version 1.0.0
 */
@SpringBootApplication
public class FormFillApiApplication {
    
    private static final Logger logger = LoggerFactory.getLogger(FormFillApiApplication.class);
    
    public static void main(String[] args) {
        logger.info("FormFillAPI service is starting...");
        logger.info("API Documentation:");
        logger.info("  POST /api/fill-form - Fill forms");
        logger.info("  GET /api/templates - Get template list");
        logger.info("  GET /api/download/{filename} - Download files");
        logger.info("  GET /api/health - Health check");
        
        SpringApplication.run(FormFillApiApplication.class, args);
        
        logger.info("FormFillAPI service started successfully!");
    }
} 