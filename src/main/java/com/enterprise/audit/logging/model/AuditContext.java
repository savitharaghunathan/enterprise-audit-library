package com.enterprise.audit.logging.model;

import org.slf4j.MDC;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides a context for storing audit-related information that should be 
 * included with audit events. This class uses SLF4J's MDC (Mapped Diagnostic Context)
 * for thread-local storage of context information.
 */
public class AuditContext {
    
    // Standard MDC keys for audit context
    public static final String USER_ID_KEY = "audit.user.id";
    public static final String SESSION_ID_KEY = "audit.session.id";
    public static final String CORRELATION_ID_KEY = "audit.correlation.id";
    public static final String APPLICATION_KEY = "audit.application";
    public static final String COMPONENT_KEY = "audit.component";
    public static final String SOURCE_IP_KEY = "audit.source.ip";
    public static final String USER_AGENT_KEY = "audit.user.agent";

    /**
     * Sets the user ID in the audit context.
     */
    public static void setUserId(String userId) {
        if (userId != null) {
            MDC.put(USER_ID_KEY, userId);
        } else {
            MDC.remove(USER_ID_KEY);
        }
    }

    /**
     * Gets the user ID from the audit context.
     */
    public static String getUserId() {
        return MDC.get(USER_ID_KEY);
    }

    /**
     * Sets the session ID in the audit context.
     */
    public static void setSessionId(String sessionId) {
        if (sessionId != null) {
            MDC.put(SESSION_ID_KEY, sessionId);
        } else {
            MDC.remove(SESSION_ID_KEY);
        }
    }

    /**
     * Gets the session ID from the audit context.
     */
    public static String getSessionId() {
        return MDC.get(SESSION_ID_KEY);
    }

    /**
     * Sets the correlation ID in the audit context.
     */
    public static void setCorrelationId(String correlationId) {
        if (correlationId != null) {
            MDC.put(CORRELATION_ID_KEY, correlationId);
        } else {
            MDC.remove(CORRELATION_ID_KEY);
        }
    }

    /**
     * Gets the correlation ID from the audit context.
     */
    public static String getCorrelationId() {
        return MDC.get(CORRELATION_ID_KEY);
    }

    /**
     * Sets the application name in the audit context.
     */
    public static void setApplication(String application) {
        if (application != null) {
            MDC.put(APPLICATION_KEY, application);
        } else {
            MDC.remove(APPLICATION_KEY);
        }
    }

    /**
     * Gets the application name from the audit context.
     */
    public static String getApplication() {
        return MDC.get(APPLICATION_KEY);
    }

    /**
     * Sets the component name in the audit context.
     */
    public static void setComponent(String component) {
        if (component != null) {
            MDC.put(COMPONENT_KEY, component);
        } else {
            MDC.remove(COMPONENT_KEY);
        }
    }

    /**
     * Gets the component name from the audit context.
     */
    public static String getComponent() {
        return MDC.get(COMPONENT_KEY);
    }

    /**
     * Sets the source IP address in the audit context.
     */
    public static void setSourceIp(String sourceIp) {
        if (sourceIp != null) {
            MDC.put(SOURCE_IP_KEY, sourceIp);
        } else {
            MDC.remove(SOURCE_IP_KEY);
        }
    }

    /**
     * Gets the source IP address from the audit context.
     */
    public static String getSourceIp() {
        return MDC.get(SOURCE_IP_KEY);
    }

    /**
     * Sets the user agent in the audit context.
     */
    public static void setUserAgent(String userAgent) {
        if (userAgent != null) {
            MDC.put(USER_AGENT_KEY, userAgent);
        } else {
            MDC.remove(USER_AGENT_KEY);
        }
    }

    /**
     * Gets the user agent from the audit context.
     */
    public static String getUserAgent() {
        return MDC.get(USER_AGENT_KEY);
    }

    /**
     * Gets all audit context values as a map.
     */
    public static Map<String, String> getContextMap() {
        Map<String, String> contextMap = new HashMap<>();
        
        String userId = getUserId();
        if (userId != null) contextMap.put("userId", userId);
        
        String sessionId = getSessionId();
        if (sessionId != null) contextMap.put("sessionId", sessionId);
        
        String correlationId = getCorrelationId();
        if (correlationId != null) contextMap.put("correlationId", correlationId);
        
        String application = getApplication();
        if (application != null) contextMap.put("application", application);
        
        String component = getComponent();
        if (component != null) contextMap.put("component", component);
        
        String sourceIp = getSourceIp();
        if (sourceIp != null) contextMap.put("sourceIp", sourceIp);
        
        String userAgent = getUserAgent();
        if (userAgent != null) contextMap.put("userAgent", userAgent);
        
        return contextMap;
    }

    /**
     * Creates an AuditEvent from the current context and provided parameters.
     */
    public static AuditEvent fromContext(
            String event_type,
            String action,
            String resource,
            AuditResult result,
            String message,
            Map<String, Object> details) {
        return new AuditEvent(
            null, // timestamp (will default to now)
            event_type,
            getUserId(),
            getSessionId(),
            getApplication(),
            getComponent(),
            action,
            resource,
            result,
            message,
            details,
            getCorrelationId(),
            getSourceIp(),
            getUserAgent()
        );
    }

    /**
     * Clears all audit context information from the current thread.
     */
    public static void clear() {
        MDC.remove(USER_ID_KEY);
        MDC.remove(SESSION_ID_KEY);
        MDC.remove(CORRELATION_ID_KEY);
        MDC.remove(APPLICATION_KEY);
        MDC.remove(COMPONENT_KEY);
        MDC.remove(SOURCE_IP_KEY);
        MDC.remove(USER_AGENT_KEY);
    }
} 