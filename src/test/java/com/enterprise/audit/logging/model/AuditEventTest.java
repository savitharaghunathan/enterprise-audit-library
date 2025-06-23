package com.enterprise.audit.logging.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.Assert.*;

public class AuditEventTest {
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Test
    public void testAuditEventCreation() {
        Instant timestamp = Instant.now();
        String eventType = "USER_LOGIN";
        String userId = "user123";
        String sessionId = "sess456";
        String application = "webapp";
        String component = "auth";
        String action = "login";
        String resource = "/api/login";
        AuditResult result = AuditResult.SUCCESS;
        String message = "User logged in successfully";
        Map<String, Object> details = Map.of("ip", "192.168.1.1", "userAgent", "Mozilla/5.0");
        String correlationId = "corr789";
        String sourceIp = "192.168.1.1";
        String userAgent = "Mozilla/5.0";

        AuditEvent event = new AuditEvent(
                timestamp, eventType, userId, sessionId, application, component,
                action, resource, result, message, details, correlationId, sourceIp, userAgent
        );

        assertEquals(timestamp, event.timestamp());
        assertEquals(eventType, event.event_type());
        assertEquals(userId, event.user_id());
        assertEquals(sessionId, event.session_id());
        assertEquals(application, event.application());
        assertEquals(component, event.component());
        assertEquals(action, event.action());
        assertEquals(resource, event.resource());
        assertEquals(result, event.result());
        assertEquals(message, event.message());
        assertEquals(details, event.details());
        assertEquals(correlationId, event.correlation_id());
        assertEquals(sourceIp, event.source_ip());
        assertEquals(userAgent, event.user_agent());
    }

    @Test
    public void testAuditEventImmutability() {
        Instant timestamp = Instant.now();
        Map<String, Object> details = Map.of("key1", "value1");
        
        AuditEvent event = new AuditEvent(
                timestamp, "TEST", "user", "session", "app", "comp",
                "action", "/resource", AuditResult.SUCCESS, "message", details, "corr", "ip", "agent"
        );

        // Verify the record is immutable - we can't modify it after creation
        // The record automatically provides equals, hashCode, and toString
        AuditEvent sameEvent = new AuditEvent(
                timestamp, "TEST", "user", "session", "app", "comp",
                "action", "/resource", AuditResult.SUCCESS, "message", details, "corr", "ip", "agent"
        );

        assertEquals(event, sameEvent);
        assertEquals(event.hashCode(), sameEvent.hashCode());
    }

    @Test
    public void testAuditEventJsonSerialization() throws Exception {
        Instant timestamp = Instant.parse("2023-01-01T12:00:00Z");
        Map<String, Object> details = Map.of("ip", "192.168.1.1", "userAgent", "Mozilla/5.0");
        
        AuditEvent event = new AuditEvent(
                timestamp, "USER_LOGIN", "user123", "sess456", "webapp", "auth",
                "login", "/api/login", AuditResult.SUCCESS, "Login successful", details, "corr789", "192.168.1.1", "Mozilla/5.0"
        );

        String json = objectMapper.writeValueAsString(event);
        
        // Should contain the epoch seconds for 2023-01-01T12:00:00Z
        assertTrue("JSON should contain timestamp as epoch seconds", json.contains("\"timestamp\":1672574400.000000000"));
        assertTrue("JSON should contain event type", json.contains("USER_LOGIN"));
        assertTrue("JSON should contain user ID", json.contains("user123"));
        assertTrue("JSON should contain result", json.contains("success"));
        assertTrue("JSON should contain details", json.contains("ip"));
        assertTrue("JSON should contain details", json.contains("192.168.1.1"));
    }

    @Test
    public void testAuditEventJsonDeserialization() throws Exception {
        String json = """
                {
                    "timestamp": 1672574400.000000000,
                    "event_type": "USER_LOGIN",
                    "user_id": "user123",
                    "session_id": "sess456",
                    "application": "webapp",
                    "component": "auth",
                    "action": "login",
                    "resource": "/api/login",
                    "result": "success",
                    "message": "Login successful",
                    "details": {"ip": "192.168.1.1", "userAgent": "Mozilla/5.0"},
                    "correlation_id": "corr789",
                    "source_ip": "192.168.1.1",
                    "user_agent": "Mozilla/5.0"
                }
                """;

        AuditEvent event = objectMapper.readValue(json, AuditEvent.class);

        assertEquals(Instant.parse("2023-01-01T12:00:00Z"), event.timestamp());
        assertEquals("USER_LOGIN", event.event_type());
        assertEquals("user123", event.user_id());
        assertEquals("sess456", event.session_id());
        assertEquals("webapp", event.application());
        assertEquals("auth", event.component());
        assertEquals("login", event.action());
        assertEquals("/api/login", event.resource());
        assertEquals(AuditResult.SUCCESS, event.result());
        assertEquals("Login successful", event.message());
        assertEquals(Map.of("ip", "192.168.1.1", "userAgent", "Mozilla/5.0"), event.details());
        assertEquals("corr789", event.correlation_id());
        assertEquals("192.168.1.1", event.source_ip());
        assertEquals("Mozilla/5.0", event.user_agent());
    }

    @Test
    public void testAuditEventToString() {
        AuditEvent event = new AuditEvent(
                Instant.parse("2023-01-01T12:00:00Z"), "TEST", "user", "session", "app", "comp",
                "action", "/resource", AuditResult.SUCCESS, "message", Map.of("key", "value"), "corr", "ip", "agent"
        );

        String toString = event.toString();
        System.out.println("AuditEvent.toString(): " + toString);
        
        assertTrue("toString should contain event type", toString.contains("TEST"));
        assertTrue("toString should contain user ID", toString.contains("user"));
        assertTrue("toString should contain result", toString.contains("success"));
        assertTrue("toString should contain timestamp", toString.contains("2023-01-01T12:00:00Z"));
    }

    @Test
    public void testAuditEventWithNullValues() {
        AuditEvent event = new AuditEvent(
                Instant.now(), "TEST", null, null, "app", "comp",
                "action", "/resource", AuditResult.SUCCESS, null, null, null, null, null
        );

        assertNull(event.user_id());
        assertNull(event.session_id());
        assertNull(event.message());
        assertNull(event.details());
        assertNull(event.correlation_id());
        assertNull(event.source_ip());
        assertNull(event.user_agent());
    }

    @Test
    public void testAuditEventDefaultTimestamp() {
        AuditEvent event = new AuditEvent(
                null, "TEST", "user", "session", "app", "comp",
                "action", "/resource", AuditResult.SUCCESS, "message", Map.of("key", "value"), "corr", "ip", "agent"
        );

        assertNotNull("Timestamp should be auto-generated when null", event.timestamp());
        assertTrue("Timestamp should be recent", 
                event.timestamp().isAfter(Instant.now().minusSeconds(5)));
    }
} 