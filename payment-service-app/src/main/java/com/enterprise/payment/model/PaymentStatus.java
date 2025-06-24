package com.enterprise.payment.model;

/**
 * Enumeration of payment processing statuses.
 */
public enum PaymentStatus {
    
    /**
     * Payment is being processed
     */
    PROCESSING("processing"),
    
    /**
     * Payment was successfully completed
     */
    COMPLETED("completed"),
    
    /**
     * Payment was declined
     */
    DECLINED("declined"),
    
    /**
     * Payment failed due to technical issues
     */
    FAILED("failed"),
    
    /**
     * Payment was cancelled
     */
    CANCELLED("cancelled"),
    
    /**
     * Payment is pending approval
     */
    PENDING("pending"),
    
    /**
     * Payment was refunded
     */
    REFUNDED("refunded");

    private final String value;

    PaymentStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
} 