package com.enterprise.audit.logging.example;

import com.enterprise.audit.logging.config.AuditConfiguration;
import com.enterprise.audit.logging.model.AuditContext;
import com.enterprise.audit.logging.model.AuditEvent;
import com.enterprise.audit.logging.model.AuditResult;
import com.enterprise.audit.logging.service.AuditLogger;
import com.enterprise.audit.logging.service.StreamableAuditLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * Example class demonstrating how to use the Enterprise Audit Logging Library (v2, Java 21+).
 */
public class AuditLoggingExample {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // Example 1: Basic usage with default configuration
        basicUsageExample();
        // Example 2: Usage with custom configuration
        customConfigurationExample();
        // Example 3: Usage with audit context
        auditContextExample();
        // Example 4: Advanced usage with detailed events
        advancedUsageExample();
    }

    public static void basicUsageExample() throws ExecutionException, InterruptedException {
        System.out.println("=== Basic Usage Example ===");
        AuditLogger auditLogger = new StreamableAuditLogger(new AuditConfiguration());
        try {
            auditLogger.logEventAsync(new AuditEvent(null, "USER_ACTION", "user1", null, "App", null, "LOGIN", "/login", AuditResult.SUCCESS, "User logged in successfully", null, null, null, null)).get();
            auditLogger.logEventAsync(new AuditEvent(null, "DATABASE", "user1", null, "App", null, "QUERY", "users_table", AuditResult.FAILURE, "Connection timeout", null, null, null, null)).get();
            auditLogger.logEventAsync(new AuditEvent(null, "SECURITY", "user1", null, "App", null, "ACCESS", "/admin", AuditResult.DENIED, "Insufficient permissions", null, null, null, null)).get();
            System.out.println("Basic audit events logged asynchronously");
        } finally {
            auditLogger.close();
        }
    }

    public static void customConfigurationExample() throws ExecutionException, InterruptedException {
        System.out.println("\n=== Custom Configuration Example ===");
        Properties props = new Properties();
        props.setProperty("audit.stream.host", "localhost");
        props.setProperty("audit.stream.port", "5044");
        AuditConfiguration config = new AuditConfiguration(props);
        AuditLogger auditLogger = new StreamableAuditLogger(config);
        try {
            auditLogger.logEventAsync(new AuditEvent(null, "APPLICATION", "user2", null, "MyApplication", null, "STARTUP", null, AuditResult.SUCCESS, "Application started successfully", null, null, null, null)).get();
            System.out.println("Custom configuration audit event logged asynchronously");
        } finally {
            auditLogger.close();
        }
    }

    public static void auditContextExample() throws ExecutionException, InterruptedException {
        System.out.println("\n=== Audit Context Example ===");
        AuditLogger auditLogger = new StreamableAuditLogger(new AuditConfiguration());
        try {
            AuditContext.setUserId("john.doe");
            AuditContext.setSessionId("session_12345");
            AuditContext.setApplication("WebPortal");
            AuditContext.setComponent("UserService");
            AuditContext.setCorrelationId("req_67890");
            AuditContext.setSourceIp("192.168.1.100");
            AuditContext.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            auditLogger.logEventAsync(AuditContext.fromContext("USER_ACTION", "VIEW_PROFILE", "/profile/john.doe", AuditResult.SUCCESS, "Profile viewed", null)).get();
            auditLogger.logEventAsync(AuditContext.fromContext("USER_ACTION", "UPDATE_PROFILE", "/profile/john.doe", AuditResult.SUCCESS, "Profile updated", null)).get();
            AuditContext.setUserId("jane.smith");
            AuditContext.setSessionId("session_54321");
            auditLogger.logEventAsync(AuditContext.fromContext("USER_ACTION", "DELETE_USER", "/admin/users/john.doe", AuditResult.DENIED, "Not authorized", null)).get();
            System.out.println("Context-aware audit events logged asynchronously");
        } finally {
            AuditContext.clear();
            auditLogger.close();
        }
    }

    public static void advancedUsageExample() throws ExecutionException, InterruptedException {
        System.out.println("\n=== Advanced Usage Example ===");
        AuditLogger auditLogger = new StreamableAuditLogger(new AuditConfiguration());
        try {
            // Example 1: Using the record for complex events
            AuditEvent complexEvent = new AuditEvent(
                null, "API_CALL", "api_user_123", "api_session_456", "RestAPI", "OrderController",
                "CREATE_ORDER", "/api/v1/orders", AuditResult.SUCCESS, "Order created successfully",
                null, "order_req_789", "10.0.0.15", "ApiClient/1.0"
            );
            auditLogger.logEventAsync(complexEvent).get();
            // Example 2: Event with additional details
            Map<String, Object> details = new HashMap<>();
            details.put("order_id", "ORD-12345");
            details.put("customer_id", "CUST-67890");
            details.put("total_amount", 299.99);
            details.put("currency", "USD");
            details.put("payment_method", "CREDIT_CARD");
            details.put("processing_time_ms", 145);
            auditLogger.logEventAsync(new AuditEvent(null, "PAYMENT", "api_user_123", null, "RestAPI", null, "PROCESS", "payment_gateway", AuditResult.SUCCESS, "Payment processed successfully", details, null, null, null)).get();
            // Example 3: Error handling and logging failures
            try {
                processOrder("INVALID_ORDER");
            } catch (Exception e) {
                Map<String, Object> errorDetails = new HashMap<>();
                errorDetails.put("error_type", e.getClass().getSimpleName());
                errorDetails.put("error_message", e.getMessage());
                errorDetails.put("stack_trace_length", e.getStackTrace().length);
                auditLogger.logEventAsync(new AuditEvent(null, "ORDER_PROCESSING", "api_user_123", null, "RestAPI", null, "VALIDATE", "order_validator", AuditResult.FAILURE, "Order validation failed", errorDetails, null, null, null)).get();
            }
            System.out.println("Advanced audit events logged asynchronously");
        } finally {
            auditLogger.close();
        }
    }

    private static void processOrder(String orderId) throws Exception {
        if ("INVALID_ORDER".equals(orderId)) {
            throw new IllegalArgumentException("Invalid order ID provided");
        }
    }
} 