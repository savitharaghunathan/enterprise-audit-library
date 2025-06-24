package com.enterprise.payment;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration tests for PaymentServiceApplication.
 * Tests that the Spring Boot application context loads successfully.
 */
@SpringBootTest
@ActiveProfiles("test")
class PaymentServiceApplicationTests {

    @Test
    void contextLoads() {
        // This test verifies that the Spring application context loads successfully
        // If there are any configuration issues, this test will fail
    }

    @Test
    void testApplicationStartup() {
        // This test verifies that the application can be started
        // In a real scenario, you might want to test specific startup behavior
        assert true; // Placeholder assertion
    }
} 