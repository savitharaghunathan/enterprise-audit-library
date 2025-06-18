package com.enterprise.audit.logging.service;

import com.enterprise.audit.logging.exception.AuditLoggingException;
import com.enterprise.audit.logging.model.AuditEvent;
import com.enterprise.audit.logging.model.AuditResult;

import java.util.Map;

/**
 * Main interface for audit logging functionality.
 * Implementations of this interface provide different backends for persisting audit events.
 */
public interface AuditLogger {

    /**
     * Logs a complete audit event.
     * 
     * @param auditEvent the audit event to log
     * @throws AuditLoggingException if the event cannot be logged
     */
    void logEvent(AuditEvent auditEvent) throws AuditLoggingException;

    /**
     * Logs an audit event with the specified parameters.
     * Context information from AuditContext will be automatically included.
     * 
     * @param eventType the type of event being audited
     * @param action the action being performed
     * @param resource the resource being accessed
     * @param result the result of the action
     * @param message descriptive message about the event
     * @throws AuditLoggingException if the event cannot be logged
     */
    void logEvent(String eventType, String action, String resource, 
                  AuditResult result, String message) throws AuditLoggingException;

    /**
     * Logs an audit event with the specified parameters and additional details.
     * Context information from AuditContext will be automatically included.
     * 
     * @param eventType the type of event being audited
     * @param action the action being performed
     * @param resource the resource being accessed
     * @param result the result of the action
     * @param message descriptive message about the event
     * @param details additional details about the event
     * @throws AuditLoggingException if the event cannot be logged
     */
    void logEvent(String eventType, String action, String resource, 
                  AuditResult result, String message, Map<String, Object> details) 
                  throws AuditLoggingException;

    /**
     * Logs a successful audit event.
     * 
     * @param eventType the type of event being audited
     * @param action the action being performed
     * @param resource the resource being accessed
     * @param message descriptive message about the event
     * @throws AuditLoggingException if the event cannot be logged
     */
    void logSuccess(String eventType, String action, String resource, String message) 
                    throws AuditLoggingException;

    /**
     * Logs a failed audit event.
     * 
     * @param eventType the type of event being audited
     * @param action the action being performed
     * @param resource the resource being accessed
     * @param message descriptive message about the event
     * @throws AuditLoggingException if the event cannot be logged
     */
    void logFailure(String eventType, String action, String resource, String message) 
                    throws AuditLoggingException;

    /**
     * Logs an access denied audit event.
     * 
     * @param eventType the type of event being audited
     * @param action the action being performed
     * @param resource the resource being accessed
     * @param message descriptive message about the event
     * @throws AuditLoggingException if the event cannot be logged
     */
    void logDenied(String eventType, String action, String resource, String message) 
                   throws AuditLoggingException;

    /**
     * Checks if the audit logger is properly configured and ready to log events.
     * 
     * @return true if the logger is ready, false otherwise
     */
    boolean isReady();

    /**
     * Closes the audit logger and releases any resources.
     * After closing, the logger should not be used for logging events.
     * 
     * @throws AuditLoggingException if there's an error during cleanup
     */
    void close() throws AuditLoggingException;
} 