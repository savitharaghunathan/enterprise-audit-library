package com.enterprise.audit.logging.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.Map;

/**
 * Modern Java 21 record for audit events (Version 2.0+)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuditEvent(
    Instant timestamp,
    String event_type,
    String user_id,
    String session_id,
    String application,
    String component,
    String action,
    String resource,
    AuditResult result,
    String message,
    Map<String, Object> details,
    String correlation_id,
    String source_ip,
    String user_agent
) {
    public AuditEvent {
        if (timestamp == null) timestamp = Instant.now();
    }
} 