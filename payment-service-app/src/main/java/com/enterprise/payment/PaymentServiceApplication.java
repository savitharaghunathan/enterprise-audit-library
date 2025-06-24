package com.enterprise.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application class for the Payment Service.
 * This service demonstrates the use of the Enterprise Audit Logging Library
 * for comprehensive audit trail of payment operations.
 */
@SpringBootApplication
public class PaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
} 