package com.enterprise.payment.service;

import com.enterprise.audit.logging.model.AuditContext;
import com.enterprise.audit.logging.model.AuditEvent;
import com.enterprise.audit.logging.model.AuditResult;
import com.enterprise.audit.logging.service.AuditLogger;
import com.enterprise.audit.logging.service.StreamableAuditLogger;
import com.enterprise.payment.model.PaymentRequest;
import com.enterprise.payment.model.PaymentResponse;
import com.enterprise.payment.model.PaymentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service for processing payment operations with comprehensive audit logging.
 * Demonstrates the use of the Enterprise Audit Logging Library v2 for compliance
 * and operational visibility with Java 21 features.
 */
@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    
    private AuditLogger auditLogger;
    
    // Simulated processing fee percentage
    private static final BigDecimal PROCESSING_FEE_PERCENTAGE = new BigDecimal("0.029"); // 2.9%
    
    // Simulated minimum processing fee
    private static final BigDecimal MIN_PROCESSING_FEE = new BigDecimal("0.30");

    @PostConstruct
    public void initialize() {
        try {
            // Initialize the audit logger with TCP streaming backend
            auditLogger = new StreamableAuditLogger();
            
            // Set up audit context for the payment service
            AuditContext.setApplication("payment-service");
            AuditContext.setComponent("payment-processor");
            
            logger.info("Payment service initialized with v2 audit logging");
            
            // Log service startup using v2 API
            auditLogger.logEventAsync(new AuditEvent(
                "SERVICE_STARTUP",
                "initialize",
                "payment-service",
                AuditResult.SUCCESS,
                "Payment service started successfully"
            ));
        } catch (Exception e) {
            logger.error("Failed to initialize payment service", e);
            throw new RuntimeException("Payment service initialization failed", e);
        }
    }

    @PreDestroy
    public void cleanup() {
        try {
            if (auditLogger != null) {
                auditLogger.logEventAsync(new AuditEvent(
                    "SERVICE_SHUTDOWN",
                    "cleanup",
                    "payment-service",
                    AuditResult.SUCCESS,
                    "Payment service shutting down"
                ));
                auditLogger.close();
            }
        } catch (Exception e) {
            logger.error("Error during payment service cleanup", e);
        }
    }

    /**
     * Process a payment request with comprehensive audit logging.
     * 
     * @param request the payment request to process
     * @return the payment response
     */
    public PaymentResponse processPayment(PaymentRequest request) {
        String correlationId = UUID.randomUUID().toString();
        
        try {
            // Set up audit context for this transaction
            AuditContext.setCorrelationId(correlationId);
            AuditContext.setUserId(request.getCustomerId());
            AuditContext.setSessionId(request.getPaymentId());
            
            // Log payment initiation using v2 API
            Map<String, Object> paymentDetails = new HashMap<>();
            paymentDetails.put("amount", request.getAmount());
            paymentDetails.put("currency", request.getCurrency());
            paymentDetails.put("payment_method", request.getPaymentMethod());
            paymentDetails.put("merchant_id", request.getMerchantId());
            
            try {
                auditLogger.logEventAsync(new AuditEvent(
                    "PAYMENT_INITIATED",
                    "process_payment",
                    "payment/" + request.getPaymentId(),
                    AuditResult.SUCCESS,
                    "Payment processing initiated",
                    paymentDetails
                ));
            } catch (Exception e) {
                logger.warn("Failed to log PAYMENT_INITIATED event", e);
            }
            
            logger.info("Processing payment: {}", request.getPaymentId());
            
            // Simulate payment processing
            PaymentResponse response = simulatePaymentProcessing(request);
            
            // Log payment result using v2 API
            Map<String, Object> resultDetails = new HashMap<>();
            resultDetails.put("transaction_id", response.getTransactionId());
            resultDetails.put("status", response.getStatus());
            resultDetails.put("processing_fee", response.getProcessingFee());
            
            try {
                if (response.getStatus() == PaymentStatus.COMPLETED) {
                    auditLogger.logEventAsync(new AuditEvent(
                        "PAYMENT_COMPLETED",
                        "process_payment",
                        "payment/" + request.getPaymentId(),
                        AuditResult.SUCCESS,
                        "Payment processed successfully"
                    ));
                } else if (response.getStatus() == PaymentStatus.DECLINED) {
                    auditLogger.logEventAsync(new AuditEvent(
                        "PAYMENT_DECLINED",
                        "process_payment",
                        "payment/" + request.getPaymentId(),
                        AuditResult.FAILURE,
                        "Payment was declined: " + response.getMessage()
                    ));
                } else {
                    auditLogger.logEventAsync(new AuditEvent(
                        "PAYMENT_PROCESSED",
                        "process_payment",
                        "payment/" + request.getPaymentId(),
                        AuditResult.SUCCESS,
                        "Payment processed with status: " + response.getStatus(),
                        resultDetails
                    ));
                }
            } catch (Exception e) {
                logger.warn("Failed to log payment result event", e);
            }
            
            logger.info("Payment processed: {} - Status: {}", 
                       request.getPaymentId(), response.getStatus());
            
            return response;
            
        } catch (Exception e) {
            // Log payment processing error using v2 API
            try {
                auditLogger.logEventAsync(new AuditEvent(
                    "PAYMENT_ERROR",
                    "process_payment",
                    "payment/" + request.getPaymentId(),
                    AuditResult.FAILURE,
                    "Payment processing failed: " + e.getMessage()
                ));
            } catch (Exception auditException) {
                logger.warn("Failed to log PAYMENT_ERROR event", auditException);
            }
            
            logger.error("Payment processing failed for: {}", request.getPaymentId(), e);
            
            // Return error response
            PaymentResponse errorResponse = new PaymentResponse();
            errorResponse.setPaymentId(request.getPaymentId());
            errorResponse.setStatus(PaymentStatus.FAILED);
            errorResponse.setMessage("Payment processing failed: " + e.getMessage());
            errorResponse.setAmount(request.getAmount());
            errorResponse.setCurrency(request.getCurrency());
            
            return errorResponse;
        } finally {
            // Clear audit context
            AuditContext.setCorrelationId(null);
            AuditContext.setUserId(null);
            AuditContext.setSessionId(null);
        }
    }

    /**
     * Simulate payment processing logic.
     * In a real application, this would integrate with payment gateways.
     */
    private PaymentResponse simulatePaymentProcessing(PaymentRequest request) {
        // Simulate processing delay
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Generate transaction ID
        String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        // Calculate processing fee
        BigDecimal processingFee = calculateProcessingFee(request.getAmount());
        
        // Simulate different payment outcomes based on amount
        PaymentStatus status;
        String message;
        
        if (request.getAmount().compareTo(new BigDecimal("10000")) > 0) {
            // Large amount - decline
            status = PaymentStatus.DECLINED;
            message = "Payment declined: Amount exceeds limit";
        } else if (request.getAmount().compareTo(new BigDecimal("0.01")) < 0) {
            // Very small amount - fail
            status = PaymentStatus.FAILED;
            message = "Payment failed: Amount too small";
        } else if (request.getPaymentMethod().equalsIgnoreCase("INVALID_CARD")) {
            // Invalid payment method - decline
            status = PaymentStatus.DECLINED;
            message = "Payment declined: Invalid payment method";
        } else {
            // Successful payment
            status = PaymentStatus.COMPLETED;
            message = "Payment processed successfully";
        }
        
        PaymentResponse response = new PaymentResponse(
            request.getPaymentId(), 
            status, 
            transactionId, 
            request.getAmount(), 
            request.getCurrency()
        );
        
        response.setProcessingFee(processingFee);
        response.setMessage(message);
        
        return response;
    }

    /**
     * Calculate processing fee based on payment amount.
     */
    private BigDecimal calculateProcessingFee(BigDecimal amount) {
        BigDecimal fee = amount.multiply(PROCESSING_FEE_PERCENTAGE);
        return fee.max(MIN_PROCESSING_FEE);
    }

    /**
     * Get payment status by payment ID.
     * 
     * @param paymentId the payment ID to look up
     * @return the payment response or null if not found
     */
    public PaymentResponse getPaymentStatus(String paymentId) {
        try {
            AuditContext.setCorrelationId(UUID.randomUUID().toString());
            
            try {
                auditLogger.logEventAsync(new AuditEvent(
                    "PAYMENT_STATUS_REQUESTED",
                    "get_status",
                    "payment/" + paymentId,
                    AuditResult.SUCCESS,
                    "Payment status requested"
                ));
            } catch (Exception e) {
                logger.warn("Failed to log PAYMENT_STATUS_REQUESTED event", e);
            }
            
            // In a real application, this would query a database
            // For demo purposes, return a mock response
            PaymentResponse response = new PaymentResponse();
            response.setPaymentId(paymentId);
            response.setStatus(PaymentStatus.COMPLETED);
            response.setTransactionId("TXN-" + paymentId.substring(0, 8).toUpperCase());
            response.setAmount(new BigDecimal("100.00"));
            response.setCurrency("USD");
            response.setMessage("Payment status retrieved");
            
            try {
                auditLogger.logEventAsync(new AuditEvent(
                    "PAYMENT_STATUS_RETRIEVED",
                    "get_status",
                    "payment/" + paymentId,
                    AuditResult.SUCCESS,
                    "Payment status retrieved successfully"
                ));
            } catch (Exception e) {
                logger.warn("Failed to log PAYMENT_STATUS_RETRIEVED event", e);
            }
            
            return response;
            
        } catch (Exception e) {
            try {
                auditLogger.logEventAsync(new AuditEvent(
                    "PAYMENT_STATUS_ERROR",
                    "get_status",
                    "payment/" + paymentId,
                    AuditResult.FAILURE,
                    "Failed to retrieve payment status: " + e.getMessage()
                ));
            } catch (Exception auditException) {
                logger.warn("Failed to log PAYMENT_STATUS_ERROR event", auditException);
            }
            throw e;
        } finally {
            AuditContext.setCorrelationId(null);
        }
    }
} 