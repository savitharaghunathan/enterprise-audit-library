package com.enterprise.payment.controller;

import com.enterprise.audit.logging.model.AuditContext;
import com.enterprise.audit.logging.model.AuditResult;
import com.enterprise.audit.logging.service.AuditLogger;
import com.enterprise.audit.logging.service.FileSystemAuditLogger;
import com.enterprise.payment.model.PaymentRequest;
import com.enterprise.payment.model.PaymentResponse;
import com.enterprise.payment.model.PaymentStatus;
import com.enterprise.payment.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for payment operations with comprehensive audit logging.
 */
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    
    @Autowired
    private PaymentService paymentService;
    
    private AuditLogger auditLogger;

    @PostConstruct
    public void initialize() {
        try {
            auditLogger = new FileSystemAuditLogger();
            logger.info("Payment controller initialized with audit logging");
        } catch (Exception e) {
            logger.error("Failed to initialize payment controller", e);
        }
    }

    /**
     * Process a payment request.
     * 
     * @param request the payment request
     * @param bindingResult validation results
     * @param httpRequest the HTTP request for audit context
     * @return the payment response
     */
    @PostMapping("/process")
    public ResponseEntity<PaymentResponse> processPayment(
            @Valid @RequestBody PaymentRequest request,
            BindingResult bindingResult,
            HttpServletRequest httpRequest) {
        
        String correlationId = UUID.randomUUID().toString();
        
        try {
            // Set up audit context
            setupAuditContext(request, httpRequest, correlationId);
            
            // Log API request
            Map<String, Object> requestDetails = new HashMap<>();
            requestDetails.put("payment_id", request.getPaymentId());
            requestDetails.put("amount", request.getAmount());
            requestDetails.put("currency", request.getCurrency());
            requestDetails.put("payment_method", request.getPaymentMethod());
            
            try {
                auditLogger.logEvent("API_REQUEST", "process_payment", 
                                   "api/payments/process", 
                                   AuditResult.SUCCESS, 
                                   "Payment processing API request received", 
                                   requestDetails);
            } catch (Exception e) {
                logger.warn("Failed to log API request audit event", e);
            }
            
            // Validate request
            if (bindingResult.hasErrors()) {
                String errorMessage = "Validation failed: " + bindingResult.getFieldErrors().get(0).getDefaultMessage();
                
                try {
                    auditLogger.logFailure("VALIDATION_ERROR", "process_payment", 
                                         "api/payments/process", 
                                         errorMessage);
                } catch (Exception e) {
                    logger.warn("Failed to log validation error audit event", e);
                }
                
                logger.warn("Payment validation failed: {}", errorMessage);
                
                PaymentResponse errorResponse = new PaymentResponse();
                errorResponse.setPaymentId(request.getPaymentId());
                errorResponse.setMessage(errorMessage);
                
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Process payment
            PaymentResponse response = paymentService.processPayment(request);
            
            // Log API response
            Map<String, Object> responseDetails = new HashMap<>();
            responseDetails.put("status", response.getStatus());
            responseDetails.put("transaction_id", response.getTransactionId());
            
            try {
                auditLogger.logEvent("API_RESPONSE", "process_payment", 
                                   "api/payments/process", 
                                   AuditResult.SUCCESS, 
                                   "Payment processing API response sent", 
                                   responseDetails);
            } catch (Exception e) {
                logger.warn("Failed to log API response audit event", e);
            }
            
            logger.info("Payment processed via API: {} - Status: {}", 
                       request.getPaymentId(), response.getStatus());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            // Log API error
            try {
                auditLogger.logFailure("API_ERROR", "process_payment", 
                                     "api/payments/process", 
                                     "API error: " + e.getMessage());
            } catch (Exception auditException) {
                logger.warn("Failed to log API error audit event", auditException);
            }
            
            logger.error("Payment API error for: {}", request.getPaymentId(), e);
            
            PaymentResponse errorResponse = new PaymentResponse();
            errorResponse.setPaymentId(request.getPaymentId());
            errorResponse.setMessage("Internal server error: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            
        } finally {
            // Clear audit context
            clearAuditContext();
        }
    }

    /**
     * Get payment status by payment ID.
     * 
     * @param paymentId the payment ID
     * @param httpRequest the HTTP request for audit context
     * @return the payment status
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPaymentStatus(@PathVariable String paymentId) {
        try {
            // For demo purposes, return a mock response
            // In a real application, this would query the database
            PaymentResponse response = new PaymentResponse();
            response.setPaymentId(paymentId);
            response.setStatus(PaymentStatus.COMPLETED);
            response.setTransactionId("TXN-" + paymentId.substring(0, Math.min(8, paymentId.length())).toUpperCase());
            response.setAmount(new BigDecimal("100.00"));
            response.setCurrency("USD");
            response.setMessage("Payment status retrieved");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error retrieving payment status for {}: {}", paymentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Health check endpoint.
     * 
     * @return health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "payment-service");
        health.put("version", "1.0.0");
        
        return ResponseEntity.ok(health);
    }

    /**
     * Set up audit context for payment processing.
     */
    private void setupAuditContext(PaymentRequest request, HttpServletRequest httpRequest, String correlationId) {
        AuditContext.setCorrelationId(correlationId);
        AuditContext.setUserId(request.getCustomerId());
        AuditContext.setSessionId(request.getPaymentId());
        AuditContext.setApplication("payment-service");
        AuditContext.setComponent("payment-api");
        AuditContext.setSourceIp(getClientIpAddress(httpRequest));
        AuditContext.setUserAgent(httpRequest.getHeader("User-Agent"));
    }

    /**
     * Set up audit context for status requests.
     */
    private void setupAuditContextForStatus(String paymentId, HttpServletRequest httpRequest, String correlationId) {
        AuditContext.setCorrelationId(correlationId);
        AuditContext.setSessionId(paymentId);
        AuditContext.setApplication("payment-service");
        AuditContext.setComponent("payment-api");
        AuditContext.setSourceIp(getClientIpAddress(httpRequest));
        AuditContext.setUserAgent(httpRequest.getHeader("User-Agent"));
    }

    /**
     * Clear audit context.
     */
    private void clearAuditContext() {
        AuditContext.setCorrelationId(null);
        AuditContext.setUserId(null);
        AuditContext.setSessionId(null);
        AuditContext.setSourceIp(null);
        AuditContext.setUserAgent(null);
    }

    /**
     * Get client IP address from HTTP request.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0];
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
} 