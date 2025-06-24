package com.enterprise.payment.gateway;

import com.enterprise.payment.model.GatewayResponse;
import com.enterprise.payment.model.PaymentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mock payment gateway that simulates realistic payment processing.
 * In a real application, this would be replaced with actual gateway implementations:
 * - StripeGateway
 * - PayPalGateway
 * - SquareGateway
 * - AdyenGateway
 */
@Component
public class MockPaymentGateway implements PaymentGateway {
    
    private static final Logger logger = LoggerFactory.getLogger(MockPaymentGateway.class);
    
    private static final String GATEWAY_NAME = "MockPaymentGateway";
    
    @Override
    public GatewayResponse processPayment(PaymentRequest request) throws GatewayException {
        logger.info("Processing payment through {}: {}", GATEWAY_NAME, request.getPaymentId());
        
        long startTime = System.currentTimeMillis();
        
        // Simulate realistic processing delay (1-3 seconds)
        try {
            Thread.sleep(1000 + (long)(Math.random() * 2000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GatewayException("Payment processing interrupted", GATEWAY_NAME, "INTERRUPTED");
        }
        
        long processingTime = System.currentTimeMillis() - startTime;
        
        // Generate gateway transaction ID
        String gatewayTransactionId = "GW-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        
        // Simulate realistic payment scenarios
        GatewayResponse response = simulatePaymentScenario(request, gatewayTransactionId, processingTime);
        
        logger.info("Payment processed by {}: {} -> {} ({}ms)", 
                   GATEWAY_NAME, request.getPaymentId(), 
                   response.getGatewayResponseCode(), processingTime);
        
        return response;
    }
    
    @Override
    public GatewayResponse refundPayment(String transactionId, BigDecimal amount) throws GatewayException {
        logger.info("Processing refund through {}: {}", GATEWAY_NAME, transactionId);
        
        // Simulate refund processing
        try {
            Thread.sleep(500 + (long)(Math.random() * 1000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GatewayException("Refund processing interrupted", GATEWAY_NAME, "INTERRUPTED");
        }
        
        GatewayResponse response = new GatewayResponse(true, transactionId, "REFUND_SUCCESS");
        response.setGatewayResponseMessage("Refund processed successfully");
        response.setGatewayTimestamp(LocalDateTime.now());
        
        return response;
    }
    
    @Override
    public GatewayResponse getTransactionStatus(String transactionId) throws GatewayException {
        logger.info("Getting transaction status from {}: {}", GATEWAY_NAME, transactionId);
        
        // Simulate status lookup
        try {
            Thread.sleep(100 + (long)(Math.random() * 200));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GatewayException("Status lookup interrupted", GATEWAY_NAME, "INTERRUPTED");
        }
        
        GatewayResponse response = new GatewayResponse(true, transactionId, "COMPLETED");
        response.setGatewayResponseMessage("Transaction completed");
        response.setGatewayTimestamp(LocalDateTime.now());
        
        return response;
    }
    
    @Override
    public boolean isAvailable() {
        // Simulate 99.9% uptime
        return Math.random() > 0.001;
    }
    
    @Override
    public String getGatewayName() {
        return GATEWAY_NAME;
    }
    
    /**
     * Simulate realistic payment scenarios based on card numbers and amounts.
     */
    private GatewayResponse simulatePaymentScenario(PaymentRequest request, String gatewayTransactionId, long processingTime) throws GatewayException {
        String cardNumber = request.getCardNumber() != null ? 
                           request.getCardNumber().replaceAll("[\\s-]", "") : "";
        
        // Simulate network timeout (1% chance)
        if (Math.random() < 0.01) {
            throw new GatewayException("Network timeout", GATEWAY_NAME, "TIMEOUT", true);
        }
        
        // Simulate gateway unavailable (0.1% chance)
        if (Math.random() < 0.001) {
            throw new GatewayException("Gateway temporarily unavailable", GATEWAY_NAME, "GATEWAY_UNAVAILABLE", true);
        }
        
        // Test card numbers for specific scenarios
        if (cardNumber.equals("4000000000000002")) {
            return createDeclinedResponse(gatewayTransactionId, "CARD_DECLINED", 
                                        "Card declined by issuer", processingTime);
        }
        
        if (cardNumber.equals("4000000000009995")) {
            return createFailedResponse(gatewayTransactionId, "INSUFFICIENT_FUNDS", 
                                      "Insufficient funds", processingTime);
        }
        
        if (cardNumber.equals("4000000000009987")) {
            return createDeclinedResponse(gatewayTransactionId, "LOST_CARD", 
                                        "Lost card", processingTime);
        }
        
        if (cardNumber.equals("4000000000009979")) {
            return createDeclinedResponse(gatewayTransactionId, "STOLEN_CARD", 
                                        "Stolen card", processingTime);
        }
        
        if (cardNumber.equals("4000000000000069")) {
            return createDeclinedResponse(gatewayTransactionId, "EXPIRED_CARD", 
                                        "Card expired", processingTime);
        }
        
        if (cardNumber.equals("4000000000000127")) {
            return createDeclinedResponse(gatewayTransactionId, "INVALID_CVV", 
                                        "Invalid security code", processingTime);
        }
        
        // Simulate fraud detection for large amounts (0.5% chance)
        if (request.getAmount().compareTo(new BigDecimal("5000")) > 0 && Math.random() < 0.005) {
            return createDeclinedResponse(gatewayTransactionId, "FRAUD_DETECTED", 
                                        "Fraud detection triggered", processingTime);
        }
        
        // Simulate insufficient funds (2% chance)
        if (Math.random() < 0.02) {
            return createFailedResponse(gatewayTransactionId, "INSUFFICIENT_FUNDS", 
                                      "Insufficient funds", processingTime);
        }
        
        // Simulate card declined by issuer (1% chance)
        if (Math.random() < 0.01) {
            return createDeclinedResponse(gatewayTransactionId, "CARD_DECLINED", 
                                        "Card declined by issuer", processingTime);
        }
        
        // Simulate expired card (0.5% chance)
        if (Math.random() < 0.005) {
            return createDeclinedResponse(gatewayTransactionId, "EXPIRED_CARD", 
                                        "Card expired", processingTime);
        }
        
        // Simulate invalid CVV (0.5% chance)
        if (Math.random() < 0.005) {
            return createDeclinedResponse(gatewayTransactionId, "INVALID_CVV", 
                                        "Invalid security code", processingTime);
        }
        
        // Check amount limits
        if (request.getAmount().compareTo(new BigDecimal("10000")) > 0) {
            return createDeclinedResponse(gatewayTransactionId, "AMOUNT_LIMIT_EXCEEDED", 
                                        "Amount exceeds daily limit", processingTime);
        }
        
        if (request.getAmount().compareTo(new BigDecimal("0.01")) < 0) {
            return createFailedResponse(gatewayTransactionId, "AMOUNT_TOO_SMALL", 
                                      "Amount too small", processingTime);
        }
        
        // Successful payment (95%+ chance)
        return createSuccessfulResponse(request, gatewayTransactionId, processingTime);
    }
    
    private GatewayResponse createSuccessfulResponse(PaymentRequest request, String gatewayTransactionId, long processingTime) {
        GatewayResponse response = new GatewayResponse(true, gatewayTransactionId, "SUCCESS");
        response.setGatewayResponseMessage("Payment authorized");
        response.setAuthorizationCode("AUTH" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        response.setAvsResult("Y"); // Address verification successful
        response.setCvvResult("M"); // CVV match
        response.setAmountAuthorized(request.getAmount());
        response.setAmountCaptured(request.getAmount());
        response.setCurrency(request.getCurrency());
        response.setProcessingTimeMs(processingTime);
        response.setGatewayFee(calculateGatewayFee(request.getAmount()));
        response.setGatewayTimestamp(LocalDateTime.now());
        response.setRiskScore((int)(Math.random() * 30)); // Low risk score
        response.setFraudIndicators(new String[0]); // No fraud indicators
        
        return response;
    }
    
    private GatewayResponse createDeclinedResponse(String gatewayTransactionId, String responseCode, String message, long processingTime) {
        GatewayResponse response = new GatewayResponse(false, gatewayTransactionId, responseCode);
        response.setGatewayResponseMessage(message);
        response.setProcessingTimeMs(processingTime);
        response.setGatewayTimestamp(LocalDateTime.now());
        response.setRiskScore((int)(50 + Math.random() * 50)); // Higher risk score
        
        return response;
    }
    
    private GatewayResponse createFailedResponse(String gatewayTransactionId, String responseCode, String message, long processingTime) {
        GatewayResponse response = new GatewayResponse(false, gatewayTransactionId, responseCode);
        response.setGatewayResponseMessage(message);
        response.setProcessingTimeMs(processingTime);
        response.setGatewayTimestamp(LocalDateTime.now());
        response.setRiskScore((int)(30 + Math.random() * 40)); // Medium risk score
        
        return response;
    }
    
    private BigDecimal calculateGatewayFee(BigDecimal amount) {
        // Simulate gateway fee: 2.9% + $0.30 (typical Stripe pricing)
        BigDecimal percentageFee = amount.multiply(new BigDecimal("0.029"));
        BigDecimal flatFee = new BigDecimal("0.30");
        return percentageFee.add(flatFee);
    }
} 