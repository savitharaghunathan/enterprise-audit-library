package com.enterprise.payment.gateway;

import com.enterprise.payment.model.PaymentRequest;
import com.enterprise.payment.model.GatewayResponse;

/**
 * Interface for payment gateway integration.
 * In a real application, this would be implemented by:
 * - StripeGateway
 * - PayPalGateway  
 * - SquareGateway
 * - AdyenGateway
 * etc.
 */
public interface PaymentGateway {
    
    /**
     * Process a payment through the payment gateway.
     * This is what actually sends the request to banks/payment networks.
     * 
     * @param request the payment request with card details
     * @return gateway response with transaction details
     * @throws GatewayException if the gateway is unavailable or returns an error
     */
    GatewayResponse processPayment(PaymentRequest request) throws GatewayException;
    
    /**
     * Refund a previously processed payment.
     * 
     * @param transactionId the original transaction ID
     * @param amount the amount to refund (null for full refund)
     * @return gateway response with refund details
     * @throws GatewayException if the gateway is unavailable or returns an error
     */
    GatewayResponse refundPayment(String transactionId, java.math.BigDecimal amount) throws GatewayException;
    
    /**
     * Get the status of a payment transaction.
     * 
     * @param transactionId the transaction ID to check
     * @return gateway response with current status
     * @throws GatewayException if the gateway is unavailable or returns an error
     */
    GatewayResponse getTransactionStatus(String transactionId) throws GatewayException;
    
    /**
     * Check if the gateway is available and healthy.
     * 
     * @return true if the gateway is available
     */
    boolean isAvailable();
    
    /**
     * Get the gateway name/identifier.
     * 
     * @return the gateway name
     */
    String getGatewayName();
} 