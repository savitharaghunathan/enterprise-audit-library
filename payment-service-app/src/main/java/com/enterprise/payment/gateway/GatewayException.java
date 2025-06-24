package com.enterprise.payment.gateway;

/**
 * Exception thrown when a payment gateway encounters an error.
 * This could be network issues, gateway downtime, or invalid responses.
 */
public class GatewayException extends Exception {
    
    private final String gatewayName;
    private final String errorCode;
    private final boolean isRetryable;
    
    public GatewayException(String message) {
        super(message);
        this.gatewayName = "Unknown";
        this.errorCode = "UNKNOWN";
        this.isRetryable = false;
    }
    
    public GatewayException(String message, String gatewayName, String errorCode) {
        super(message);
        this.gatewayName = gatewayName;
        this.errorCode = errorCode;
        this.isRetryable = isRetryableError(errorCode);
    }
    
    public GatewayException(String message, String gatewayName, String errorCode, boolean isRetryable) {
        super(message);
        this.gatewayName = gatewayName;
        this.errorCode = errorCode;
        this.isRetryable = isRetryable;
    }
    
    public GatewayException(String message, Throwable cause, String gatewayName, String errorCode) {
        super(message, cause);
        this.gatewayName = gatewayName;
        this.errorCode = errorCode;
        this.isRetryable = isRetryableError(errorCode);
    }
    
    public String getGatewayName() {
        return gatewayName;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public boolean isRetryable() {
        return isRetryable;
    }
    
    /**
     * Determine if an error code represents a retryable error.
     * Network timeouts, temporary gateway issues, etc. are retryable.
     */
    private boolean isRetryableError(String errorCode) {
        if (errorCode == null) return false;
        
        return errorCode.equals("TIMEOUT") ||
               errorCode.equals("GATEWAY_UNAVAILABLE") ||
               errorCode.equals("NETWORK_ERROR") ||
               errorCode.equals("RATE_LIMIT_EXCEEDED") ||
               errorCode.equals("TEMPORARY_ERROR");
    }
} 