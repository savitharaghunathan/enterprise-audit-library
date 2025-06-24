package com.enterprise.payment.service;

import com.enterprise.audit.logging.model.AuditContext;
import com.enterprise.audit.logging.model.AuditEvent;
import com.enterprise.audit.logging.model.AuditResult;
import com.enterprise.audit.logging.service.AuditLogger;
import com.enterprise.payment.gateway.GatewayException;
import com.enterprise.payment.gateway.PaymentGateway;
import com.enterprise.payment.model.GatewayResponse;
import com.enterprise.payment.model.PaymentRequest;
import com.enterprise.payment.model.PaymentResponse;
import com.enterprise.payment.model.PaymentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Payment service that processes payments through payment gateways.
 * In a real application, this would integrate with Stripe, PayPal, etc.
 */
@Service
public class PaymentService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    
    // Simulated processing fee percentage
    private static final BigDecimal PROCESSING_FEE_PERCENTAGE = new BigDecimal("0.029"); // 2.9%
    
    // Simulated minimum processing fee
    private static final BigDecimal MIN_PROCESSING_FEE = new BigDecimal("0.30");
    
    private static final Set<String> SUPPORTED_PAYMENT_METHODS = new HashSet<>();
    static {
        SUPPORTED_PAYMENT_METHODS.add("CREDIT_CARD");
        SUPPORTED_PAYMENT_METHODS.add("DEBIT_CARD");
        SUPPORTED_PAYMENT_METHODS.add("BANK_TRANSFER");
        SUPPORTED_PAYMENT_METHODS.add("DIGITAL_WALLET");
        SUPPORTED_PAYMENT_METHODS.add("CRYPTO");
    }
    
    private static final Set<String> SUPPORTED_CURRENCIES = new HashSet<>();
    static {
        SUPPORTED_CURRENCIES.add("USD");
        SUPPORTED_CURRENCIES.add("EUR");
        SUPPORTED_CURRENCIES.add("GBP");
        SUPPORTED_CURRENCIES.add("CAD");
        SUPPORTED_CURRENCIES.add("AUD");
        SUPPORTED_CURRENCIES.add("JPY");
        SUPPORTED_CURRENCIES.add("CHF");
        SUPPORTED_CURRENCIES.add("SEK");
        SUPPORTED_CURRENCIES.add("NOK");
        SUPPORTED_CURRENCIES.add("DKK");
    }
    
    private AuditLogger auditLogger;
    
    private PaymentGateway paymentGateway;
    
    @PostConstruct
    public void init() {
        logger.info("Payment service initialized");
        
        // Initialize audit logger manually (v1 style)
        try {
            auditLogger = new com.enterprise.audit.logging.service.FileSystemAuditLogger();
            logger.info("Audit logger initialized");
        } catch (Exception e) {
            logger.error("Failed to initialize audit logger", e);
            throw new RuntimeException("Audit logger initialization failed", e);
        }
        
        // Initialize payment gateway manually
        try {
            paymentGateway = new com.enterprise.payment.gateway.MockPaymentGateway();
            logger.info("Payment gateway initialized: {}", paymentGateway.getGatewayName());
        } catch (Exception e) {
            logger.error("Failed to initialize payment gateway", e);
            throw new RuntimeException("Payment gateway initialization failed", e);
        }
        
        // Verify gateway is available
        if (!paymentGateway.isAvailable()) {
            logger.error("Payment gateway is not available - service cannot start");
            throw new RuntimeException("Payment gateway unavailable");
        }
        
        logger.info("Payment gateway '{}' is available", paymentGateway.getGatewayName());
    }
    
    @PreDestroy
    public void cleanup() {
        logger.info("Payment service shutting down");
    }
    
    /**
     * Process a payment request through the payment gateway.
     * This is the main method that handles the entire payment flow.
     */
    public PaymentResponse processPayment(PaymentRequest request) {
        logger.info("Processing payment request: {}", request.getPaymentId());
        
        // Create audit context
        Map<String, Object> auditContext = new HashMap<>();
        auditContext.put("payment_id", request.getPaymentId());
        auditContext.put("amount", request.getAmount().toString());
        auditContext.put("currency", request.getCurrency());
        auditContext.put("payment_method", request.getPaymentMethod());
        
        try {
            // Log payment initiation
            auditLogger.logEvent(
                "PAYMENT_INITIATED",
                "process_payment",
                "payment/" + request.getPaymentId(),
                AuditResult.SUCCESS,
                "Payment processing initiated",
                auditContext
            );
            
            // Validate payment request
            validatePaymentRequest(request);
            
            // Process payment through gateway
            GatewayResponse gatewayResponse = paymentGateway.processPayment(request);
            
            // Convert gateway response to payment response
            PaymentResponse response = convertGatewayResponse(request, gatewayResponse);
            
            // Log payment processing result based on actual status
            AuditResult auditResult;
            String auditMessage;
            
            if (response.getStatus() == PaymentStatus.COMPLETED) {
                auditResult = AuditResult.SUCCESS;
                auditMessage = "Payment processed successfully";
            } else if (response.getStatus() == PaymentStatus.DECLINED) {
                auditResult = AuditResult.FAILURE;
                auditMessage = "Payment declined: " + response.getMessage();
            } else {
                auditResult = AuditResult.FAILURE;
                auditMessage = "Payment failed: " + response.getMessage();
            }
            
            // Add response details to audit context
            auditContext.put("transaction_id", response.getTransactionId());
            auditContext.put("payment_status", response.getStatus().toString());
            
            auditLogger.logEvent(
                "PAYMENT_PROCESSED",
                "process_payment",
                "payment/" + request.getPaymentId(),
                auditResult,
                auditMessage,
                auditContext
            );
            
            logger.info("Payment processed: {} -> {} ({})", 
                       request.getPaymentId(), response.getStatus(), auditMessage);
            
            return response;
            
        } catch (GatewayException e) {
            logger.error("Payment gateway error for payment {}: {}", 
                        request.getPaymentId(), e.getMessage());
            
            // Log gateway failure
            try {
                auditLogger.logFailure(
                    "PAYMENT_GATEWAY_ERROR",
                    "process_payment",
                    "payment/" + request.getPaymentId(),
                    "Gateway error: " + e.getMessage()
                );
            } catch (Exception auditException) {
                logger.warn("Failed to log gateway error audit event", auditException);
            }
            
            // Return error response
            return createErrorResponse(request, e);
            
        } catch (Exception e) {
            logger.error("Unexpected error processing payment {}: {}", 
                        request.getPaymentId(), e.getMessage(), e);
            
            // Log unexpected error
            try {
                auditLogger.logFailure(
                    "PAYMENT_ERROR",
                    "process_payment",
                    "payment/" + request.getPaymentId(),
                    "Unexpected error: " + e.getMessage()
                );
            } catch (Exception auditException) {
                logger.warn("Failed to log payment error audit event", auditException);
            }
            
            // Return error response
            return createErrorResponse(request, e);
        }
    }
    
    /**
     * Validate payment request before processing.
     */
    private void validatePaymentRequest(PaymentRequest request) {
        // Validate payment method
        if (!SUPPORTED_PAYMENT_METHODS.contains(request.getPaymentMethod().toUpperCase())) {
            throw new IllegalArgumentException("Unsupported payment method: " + request.getPaymentMethod());
        }
        
        // Validate currency
        if (!SUPPORTED_CURRENCIES.contains(request.getCurrency().toUpperCase())) {
            throw new IllegalArgumentException("Unsupported currency: " + request.getCurrency());
        }
        
        // Validate amount
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        
        // Validate card details for credit/debit card payments
        if (request.getPaymentMethod().equalsIgnoreCase("CREDIT_CARD") || 
            request.getPaymentMethod().equalsIgnoreCase("DEBIT_CARD")) {
            
            if (request.getCardNumber() == null || request.getCardNumber().trim().isEmpty()) {
                throw new IllegalArgumentException("Card number is required");
            }
            
            if (request.getExpiryMonth() == null || request.getExpiryYear() == null) {
                throw new IllegalArgumentException("Card expiry date is required");
            }
            
            if (request.getCvv() == null || request.getCvv().trim().isEmpty()) {
                throw new IllegalArgumentException("CVV is required");
            }
            
            if (request.getCardholderName() == null || request.getCardholderName().trim().isEmpty()) {
                throw new IllegalArgumentException("Cardholder name is required");
            }
        }
    }
    
    /**
     * Convert gateway response to payment response.
     */
    private PaymentResponse convertGatewayResponse(PaymentRequest request, GatewayResponse gatewayResponse) {
        PaymentStatus status;
        
        if (gatewayResponse.isSuccess()) {
            status = PaymentStatus.COMPLETED;
        } else {
            // Map gateway error codes to payment status
            String errorCode = gatewayResponse.getGatewayResponseCode();
            if (errorCode != null && (errorCode.contains("DECLINED") || errorCode.contains("INSUFFICIENT"))) {
                status = PaymentStatus.DECLINED;
            } else {
                status = PaymentStatus.FAILED;
            }
        }
        
        PaymentResponse response = new PaymentResponse(
            request.getPaymentId(),
            status,
            gatewayResponse.getGatewayTransactionId(),
            request.getAmount(),
            request.getCurrency()
        );
        
        response.setProcessingFee(calculateProcessingFee(request.getAmount()));
        response.setMessage(gatewayResponse.getGatewayResponseMessage());
        
        return response;
    }
    
    /**
     * Create error response for failed payments.
     */
    private PaymentResponse createErrorResponse(PaymentRequest request, Exception error) {
        PaymentResponse response = new PaymentResponse(
            request.getPaymentId(),
            PaymentStatus.FAILED,
            "ERROR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
            request.getAmount(),
            request.getCurrency()
        );
        
        response.setProcessingFee(BigDecimal.ZERO);
        response.setMessage("Payment failed: " + error.getMessage());
        
        return response;
    }
    
    /**
     * Calculate processing fee based on payment amount.
     */
    private BigDecimal calculateProcessingFee(BigDecimal amount) {
        BigDecimal fee = amount.multiply(PROCESSING_FEE_PERCENTAGE);
        return fee.max(MIN_PROCESSING_FEE);
    }
} 