package com.enterprise.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Payment Service Application v2.
 * 
 * A Spring Boot application that demonstrates payment processing with comprehensive
 * audit logging using the Enterprise Audit Logging Library v2.
 * 
 * Features:
 * - RESTful payment processing API
 * - Comprehensive audit logging with TCP streaming backend
 * - Java 21 with virtual threads for high performance
 * - Modern Spring Boot 3.x framework
 * - Input validation and error handling
 * 
 * @author Enterprise Development Team
 * @version 2.0.0
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.enterprise.payment",
    "com.enterprise.audit.logging"
})
public class PaymentServiceApplication {

    /**
     * Main method to start the Payment Service application.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        System.out.println("Starting Payment Service Application v2.0.0...");
        System.out.println("Java Version: " + System.getProperty("java.version"));
        System.out.println("Java Home: " + System.getProperty("java.home"));
        
        SpringApplication.run(PaymentServiceApplication.class, args);
        
        System.out.println("Payment Service Application started successfully!");
        System.out.println("API Documentation: http://localhost:8080/api/v1/payments/health");
    }
} 