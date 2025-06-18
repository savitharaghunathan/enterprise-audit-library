package com.enterprise.audit.logging.exception;

/**
 * Exception thrown when there are errors in audit logging operations.
 * This is a checked exception to ensure that calling code handles audit logging failures appropriately.
 */
public class AuditLoggingException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new audit logging exception with null as its detail message.
     */
    public AuditLoggingException() {
        super();
    }

    /**
     * Constructs a new audit logging exception with the specified detail message.
     * 
     * @param message the detail message
     */
    public AuditLoggingException(String message) {
        super(message);
    }

    /**
     * Constructs a new audit logging exception with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause
     */
    public AuditLoggingException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new audit logging exception with the specified cause.
     * 
     * @param cause the cause
     */
    public AuditLoggingException(Throwable cause) {
        super(cause);
    }
} 