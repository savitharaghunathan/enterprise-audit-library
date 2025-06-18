package com.enterprise.audit.logging.example;

import com.enterprise.audit.logging.config.AuditConfiguration;
import com.enterprise.audit.logging.exception.AuditLoggingException;
import com.enterprise.audit.logging.model.AuditContext;
import com.enterprise.audit.logging.model.AuditEvent;
import com.enterprise.audit.logging.model.AuditResult;
import com.enterprise.audit.logging.service.AuditLogger;
import com.enterprise.audit.logging.service.FileSystemAuditLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Example class demonstrating how to use the Enterprise Audit Logging Library.
 * This example shows various ways to integrate audit logging into your application.
 */
public class AuditLoggingExample {

    public static void main(String[] args) {
        try {
            // Example 1: Basic usage with default configuration
            basicUsageExample();
            
            // Example 2: Usage with custom configuration
            customConfigurationExample();
            
            // Example 3: Usage with audit context
            auditContextExample();
            
            // Example 4: Advanced usage with detailed events
            advancedUsageExample();
            
        } catch (AuditLoggingException e) {
            System.err.println("Audit logging error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Example 1: Basic usage with default configuration
     */
    public static void basicUsageExample() throws AuditLoggingException {
        System.out.println("=== Basic Usage Example ===");
        
        // Create audit logger with default configuration
        AuditLogger auditLogger = new FileSystemAuditLogger();
        
        try {
            // Log different types of events
            auditLogger.logSuccess("USER_ACTION", "LOGIN", "/login", "User logged in successfully");
            auditLogger.logFailure("DATABASE", "QUERY", "users_table", "Connection timeout");
            auditLogger.logDenied("SECURITY", "ACCESS", "/admin", "Insufficient permissions");
            
            System.out.println("Basic audit events logged successfully");
            
        } finally {
            auditLogger.close();
        }
    }

    /**
     * Example 2: Usage with custom configuration
     */
    public static void customConfigurationExample() throws AuditLoggingException {
        System.out.println("\n=== Custom Configuration Example ===");
        
        // Create custom configuration
        Properties props = new Properties();
        props.setProperty("audit.log.directory", "./custom-audit-logs");
        props.setProperty("audit.log.file.prefix", "myapp-audit");
        props.setProperty("audit.log.max.file.size.mb", "50");
        props.setProperty("audit.log.auto.create.directory", "true");
        
        AuditConfiguration config = new AuditConfiguration(props);
        
        // Create audit logger with custom configuration
        AuditLogger auditLogger = new FileSystemAuditLogger(config);
        
        try {
            auditLogger.logSuccess("APPLICATION", "STARTUP", "MyApplication", "Application started successfully");
            System.out.println("Custom configuration audit event logged successfully");
            System.out.println("Log file location: " + config.getLogFilePath());
            
        } finally {
            auditLogger.close();
        }
    }

    /**
     * Example 3: Usage with audit context
     */
    public static void auditContextExample() throws AuditLoggingException {
        System.out.println("\n=== Audit Context Example ===");
        
        AuditLogger auditLogger = new FileSystemAuditLogger();
        
        try {
            // Set audit context - this information will be automatically included in all events
            AuditContext.setUserId("john.doe");
            AuditContext.setSessionId("session_12345");
            AuditContext.setApplication("WebPortal");
            AuditContext.setComponent("UserService");
            AuditContext.setCorrelationId("req_67890");
            AuditContext.setSourceIp("192.168.1.100");
            AuditContext.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            
            // Log events - context information will be automatically included
            auditLogger.logSuccess("USER_ACTION", "VIEW_PROFILE", "/profile/john.doe", "Profile viewed");
            auditLogger.logSuccess("USER_ACTION", "UPDATE_PROFILE", "/profile/john.doe", "Profile updated");
            
            // Change user context
            AuditContext.setUserId("jane.smith");
            AuditContext.setSessionId("session_54321");
            auditLogger.logDenied("USER_ACTION", "DELETE_USER", "/admin/users/john.doe", "Not authorized");
            
            System.out.println("Context-aware audit events logged successfully");
            
        } finally {
            // Clear audit context when done
            AuditContext.clear();
            auditLogger.close();
        }
    }

    /**
     * Example 4: Advanced usage with detailed events
     */
    public static void advancedUsageExample() throws AuditLoggingException {
        System.out.println("\n=== Advanced Usage Example ===");
        
        AuditLogger auditLogger = new FileSystemAuditLogger();
        
        try {
            // Example 1: Using the builder pattern for complex events
            AuditEvent complexEvent = AuditEvent.builder()
                    .eventType("API_CALL")
                    .userId("api_user_123")
                    .sessionId("api_session_456")
                    .application("RestAPI")
                    .component("OrderController")
                    .action("CREATE_ORDER")
                    .resource("/api/v1/orders")
                    .result(AuditResult.SUCCESS)
                    .message("Order created successfully")
                    .correlationId("order_req_789")
                    .sourceIp("10.0.0.15")
                    .userAgent("ApiClient/1.0")
                    .build();
            
            auditLogger.logEvent(complexEvent);
            
            // Example 2: Event with additional details
            Map<String, Object> details = new HashMap<>();
            details.put("order_id", "ORD-12345");
            details.put("customer_id", "CUST-67890");
            details.put("total_amount", 299.99);
            details.put("currency", "USD");
            details.put("payment_method", "CREDIT_CARD");
            details.put("processing_time_ms", 145);
            
            auditLogger.logEvent("PAYMENT", "PROCESS", "payment_gateway", 
                               AuditResult.SUCCESS, "Payment processed successfully", details);
            
            // Example 3: Error handling and logging failures
            try {
                // Simulate a business operation that might fail
                processOrder("INVALID_ORDER");
            } catch (Exception e) {
                Map<String, Object> errorDetails = new HashMap<>();
                errorDetails.put("error_type", e.getClass().getSimpleName());
                errorDetails.put("error_message", e.getMessage());
                errorDetails.put("stack_trace_length", e.getStackTrace().length);
                
                auditLogger.logEvent("ORDER_PROCESSING", "VALIDATE", "order_validator", 
                                   AuditResult.FAILURE, "Order validation failed", errorDetails);
            }
            
            System.out.println("Advanced audit events logged successfully");
            
        } finally {
            auditLogger.close();
        }
    }

    /**
     * Simulated business method that might throw an exception
     */
    private static void processOrder(String orderId) throws Exception {
        if ("INVALID_ORDER".equals(orderId)) {
            throw new IllegalArgumentException("Invalid order ID provided");
        }
        // Normal processing would happen here
    }

    /**
     * Example of how to integrate audit logging into a web application filter or interceptor
     */
    public static class WebRequestAuditExample {
        
        private final AuditLogger auditLogger;
        
        public WebRequestAuditExample() throws AuditLoggingException {
            this.auditLogger = new FileSystemAuditLogger();
        }
        
        public void auditWebRequest(String userId, String sessionId, String method, 
                                  String path, int statusCode, long durationMs) {
            
            try {
                // Set request context
                AuditContext.setUserId(userId);
                AuditContext.setSessionId(sessionId);
                AuditContext.setApplication("WebApplication");
                AuditContext.setComponent("WebFilter");
                
                // Determine audit result based on HTTP status
                AuditResult result;
                if (statusCode >= 200 && statusCode < 300) {
                    result = AuditResult.SUCCESS;
                } else if (statusCode == 401 || statusCode == 403) {
                    result = AuditResult.DENIED;
                } else {
                    result = AuditResult.FAILURE;
                }
                
                // Create details map
                Map<String, Object> details = new HashMap<>();
                details.put("http_method", method);
                details.put("status_code", statusCode);
                details.put("duration_ms", durationMs);
                
                // Log the request
                auditLogger.logEvent("WEB_REQUEST", method, path, result, 
                                   "HTTP request processed", details);
                
            } catch (AuditLoggingException e) {
                System.err.println("Failed to audit web request: " + e.getMessage());
            } finally {
                AuditContext.clear();
            }
        }
        
        public void close() throws AuditLoggingException {
            auditLogger.close();
        }
    }
} 