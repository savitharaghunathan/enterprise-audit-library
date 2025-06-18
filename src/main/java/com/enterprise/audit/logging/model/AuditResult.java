package com.enterprise.audit.logging.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Represents the result or outcome of an audited action.
 * This enum provides standard result codes for audit events.
 */
public enum AuditResult {
    
    /**
     * The audited action completed successfully.
     */
    SUCCESS("success"),
    
    /**
     * The audited action failed due to an error.
     */
    FAILURE("failure"),
    
    /**
     * The audited action was denied due to insufficient permissions.
     */
    DENIED("denied"),
    
    /**
     * The audited action was attempted with invalid or malformed input.
     */
    INVALID("invalid"),
    
    /**
     * The audited action timed out.
     */
    TIMEOUT("timeout"),
    
    /**
     * The audited action was cancelled or aborted.
     */
    CANCELLED("cancelled"),
    
    /**
     * The audited action had an unknown or unspecified result.
     */
    UNKNOWN("unknown");

    private final String value;

    AuditResult(String value) {
        this.value = value;
    }

    /**
     * Returns the string representation of the audit result.
     * This method is used by Jackson for JSON serialization.
     */
    @JsonValue
    public String getValue() {
        return value;
    }

    /**
     * Returns the AuditResult enum constant for the given string value.
     * 
     * @param value the string value to convert
     * @return the corresponding AuditResult enum constant
     * @throws IllegalArgumentException if no matching enum constant is found
     */
    public static AuditResult fromValue(String value) {
        if (value == null) {
            return null;
        }
        
        for (AuditResult result : AuditResult.values()) {
            if (result.value.equalsIgnoreCase(value)) {
                return result;
            }
        }
        
        throw new IllegalArgumentException("Unknown audit result value: " + value);
    }

    @Override
    public String toString() {
        return value;
    }
} 