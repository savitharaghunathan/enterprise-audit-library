package com.enterprise.audit.logging.service;

import com.enterprise.audit.logging.exception.AuditLoggingException;
import com.enterprise.audit.logging.model.AuditEvent;

import java.util.concurrent.CompletableFuture;

/**
 * Modern interface for audit logging functionality (Java 21+).
 * Provides async, streamable logging for cloud-native environments.
 */
public interface AuditLogger {
    /**
     * Asynchronously logs an audit event.
     * @param auditEvent the audit event to log
     * @return a CompletableFuture that completes when the event is sent
     */
    CompletableFuture<Void> logEventAsync(AuditEvent auditEvent);

    /**
     * Asynchronously logs an audit event with error handling.
     * @param auditEvent the audit event to log
     * @return a CompletableFuture that completes exceptionally if logging fails
     */
    CompletableFuture<Void> logEventAsyncSafe(AuditEvent auditEvent);

    /**
     * Checks if the audit logger is ready to log events.
     * @return true if ready, false otherwise
     */
    boolean isReady();

    /**
     * Closes the audit logger and releases any resources.
     */
    void close();

    /**
     * @deprecated Use logEventAsync instead.
     */
    @Deprecated
    void logEvent(AuditEvent auditEvent) throws AuditLoggingException;
} 