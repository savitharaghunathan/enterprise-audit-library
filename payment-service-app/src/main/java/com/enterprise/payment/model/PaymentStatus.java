package com.enterprise.payment.model;

/**
 * Payment status enumeration.
 * Represents the various states a payment can be in during processing.
 */
public enum PaymentStatus {

    /**
     * Payment has been successfully processed and completed.
     */
    COMPLETED("completed"),

    /**
     * Payment is currently being processed.
     */
    PENDING("pending"),

    /**
     * Payment was declined by the payment processor or bank.
     */
    DECLINED("declined"),

    /**
     * Payment processing failed due to technical issues.
     */
    FAILED("failed"),

    /**
     * Payment has been cancelled by the user or merchant.
     */
    CANCELLED("cancelled"),

    /**
     * Payment is being refunded.
     */
    REFUNDING("refunding"),

    /**
     * Payment has been fully refunded.
     */
    REFUNDED("refunded"),

    /**
     * Payment is being disputed or under review.
     */
    DISPUTED("disputed");

    private final String value;

    PaymentStatus(String value) {
        this.value = value;
    }

    /**
     * Get the string representation of the payment status.
     * 
     * @return the status value as a string
     */
    public String getValue() {
        return value;
    }

    /**
     * Get a PaymentStatus from its string value.
     * 
     * @param value the string value
     * @return the corresponding PaymentStatus, or null if not found
     */
    public static PaymentStatus fromValue(String value) {
        for (PaymentStatus status : PaymentStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        return null;
    }

    /**
     * Check if the payment status represents a successful outcome.
     * 
     * @return true if the status is COMPLETED, false otherwise
     */
    public boolean isSuccessful() {
        return this == COMPLETED;
    }

    /**
     * Check if the payment status represents a terminal state.
     * 
     * @return true if the status is terminal (COMPLETED, DECLINED, FAILED, CANCELLED, REFUNDED), false otherwise
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == DECLINED || this == FAILED || 
               this == CANCELLED || this == REFUNDED;
    }

    /**
     * Check if the payment status represents a failure state.
     * 
     * @return true if the status is a failure (DECLINED, FAILED), false otherwise
     */
    public boolean isFailure() {
        return this == DECLINED || this == FAILED;
    }

    @Override
    public String toString() {
        return value;
    }
} 