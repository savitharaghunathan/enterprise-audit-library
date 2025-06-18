package com.enterprise.audit.logging.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Represents an audit event that can be logged to various backends.
 * This class is designed to be serializable to JSON format for log shipping.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuditEvent {
    
    @JsonProperty("timestamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant timestamp;
    
    @JsonProperty("event_type")
    private String eventType;
    
    @JsonProperty("user_id")
    private String userId;
    
    @JsonProperty("session_id")
    private String sessionId;
    
    @JsonProperty("application")
    private String application;
    
    @JsonProperty("component")
    private String component;
    
    @JsonProperty("action")
    private String action;
    
    @JsonProperty("resource")
    private String resource;
    
    @JsonProperty("result")
    private AuditResult result;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("details")
    private Map<String, Object> details;
    
    @JsonProperty("correlation_id")
    private String correlationId;
    
    @JsonProperty("source_ip")
    private String sourceIp;
    
    @JsonProperty("user_agent")
    private String userAgent;

    // Default constructor
    public AuditEvent() {
        this.timestamp = Instant.now();
    }

    // Builder pattern constructor
    private AuditEvent(Builder builder) {
        this.timestamp = builder.timestamp != null ? builder.timestamp : Instant.now();
        this.eventType = builder.eventType;
        this.userId = builder.userId;
        this.sessionId = builder.sessionId;
        this.application = builder.application;
        this.component = builder.component;
        this.action = builder.action;
        this.resource = builder.resource;
        this.result = builder.result;
        this.message = builder.message;
        this.details = builder.details;
        this.correlationId = builder.correlationId;
        this.sourceIp = builder.sourceIp;
        this.userAgent = builder.userAgent;
    }

    // Getters and Setters
    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public AuditResult getResult() {
        return result;
    }

    public void setResult(AuditResult result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditEvent that = (AuditEvent) o;
        return Objects.equals(timestamp, that.timestamp) &&
               Objects.equals(eventType, that.eventType) &&
               Objects.equals(userId, that.userId) &&
               Objects.equals(sessionId, that.sessionId) &&
               Objects.equals(application, that.application) &&
               Objects.equals(component, that.component) &&
               Objects.equals(action, that.action) &&
               Objects.equals(resource, that.resource) &&
               result == that.result &&
               Objects.equals(message, that.message) &&
               Objects.equals(details, that.details) &&
               Objects.equals(correlationId, that.correlationId) &&
               Objects.equals(sourceIp, that.sourceIp) &&
               Objects.equals(userAgent, that.userAgent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, eventType, userId, sessionId, application, 
                          component, action, resource, result, message, details, 
                          correlationId, sourceIp, userAgent);
    }

    @Override
    public String toString() {
        return "AuditEvent{" +
               "timestamp=" + timestamp +
               ", eventType='" + eventType + '\'' +
               ", userId='" + userId + '\'' +
               ", sessionId='" + sessionId + '\'' +
               ", application='" + application + '\'' +
               ", component='" + component + '\'' +
               ", action='" + action + '\'' +
               ", resource='" + resource + '\'' +
               ", result=" + result +
               ", message='" + message + '\'' +
               ", correlationId='" + correlationId + '\'' +
               ", sourceIp='" + sourceIp + '\'' +
               ", userAgent='" + userAgent + '\'' +
               '}';
    }

    /**
     * Creates a new builder for constructing AuditEvent instances.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class for constructing AuditEvent instances using the builder pattern.
     */
    public static class Builder {
        private Instant timestamp;
        private String eventType;
        private String userId;
        private String sessionId;
        private String application;
        private String component;
        private String action;
        private String resource;
        private AuditResult result;
        private String message;
        private Map<String, Object> details;
        private String correlationId;
        private String sourceIp;
        private String userAgent;

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder eventType(String eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder application(String application) {
            this.application = application;
            return this;
        }

        public Builder component(String component) {
            this.component = component;
            return this;
        }

        public Builder action(String action) {
            this.action = action;
            return this;
        }

        public Builder resource(String resource) {
            this.resource = resource;
            return this;
        }

        public Builder result(AuditResult result) {
            this.result = result;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder details(Map<String, Object> details) {
            this.details = details;
            return this;
        }

        public Builder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public Builder sourceIp(String sourceIp) {
            this.sourceIp = sourceIp;
            return this;
        }

        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public AuditEvent build() {
            return new AuditEvent(this);
        }
    }
} 