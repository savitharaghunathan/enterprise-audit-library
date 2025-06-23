package com.enterprise.audit.logging.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.MDC;

import java.util.Map;

import static org.junit.Assert.*;

public class AuditContextTest {

    @Before
    public void setUp() {
        // Clear any existing MDC context before each test
        AuditContext.clear();
    }

    @After
    public void tearDown() {
        // Clean up MDC context after each test
        AuditContext.clear();
    }

    @Test
    public void testSetAndGetUserId() {
        String userId = "user123";
        AuditContext.setUserId(userId);
        assertEquals(userId, AuditContext.getUserId());
    }

    @Test
    public void testSetAndGetSessionId() {
        String sessionId = "sess456";
        AuditContext.setSessionId(sessionId);
        assertEquals(sessionId, AuditContext.getSessionId());
    }

    @Test
    public void testSetAndGetCorrelationId() {
        String correlationId = "corr789";
        AuditContext.setCorrelationId(correlationId);
        assertEquals(correlationId, AuditContext.getCorrelationId());
    }

    @Test
    public void testSetAndGetApplication() {
        String application = "webapp";
        AuditContext.setApplication(application);
        assertEquals(application, AuditContext.getApplication());
    }

    @Test
    public void testSetAndGetComponent() {
        String component = "auth";
        AuditContext.setComponent(component);
        assertEquals(component, AuditContext.getComponent());
    }

    @Test
    public void testSetAndGetSourceIp() {
        String sourceIp = "192.168.1.1";
        AuditContext.setSourceIp(sourceIp);
        assertEquals(sourceIp, AuditContext.getSourceIp());
    }

    @Test
    public void testSetAndGetUserAgent() {
        String userAgent = "Mozilla/5.0";
        AuditContext.setUserAgent(userAgent);
        assertEquals(userAgent, AuditContext.getUserAgent());
    }

    @Test
    public void testSetNullValues() {
        // Set values first
        AuditContext.setUserId("user123");
        AuditContext.setSessionId("sess456");
        AuditContext.setCorrelationId("corr789");
        AuditContext.setApplication("webapp");
        AuditContext.setComponent("auth");
        AuditContext.setSourceIp("192.168.1.1");
        AuditContext.setUserAgent("Mozilla/5.0");

        // Verify values are set
        assertNotNull(AuditContext.getUserId());
        assertNotNull(AuditContext.getSessionId());
        assertNotNull(AuditContext.getCorrelationId());
        assertNotNull(AuditContext.getApplication());
        assertNotNull(AuditContext.getComponent());
        assertNotNull(AuditContext.getSourceIp());
        assertNotNull(AuditContext.getUserAgent());

        // Set null values
        AuditContext.setUserId(null);
        AuditContext.setSessionId(null);
        AuditContext.setCorrelationId(null);
        AuditContext.setApplication(null);
        AuditContext.setComponent(null);
        AuditContext.setSourceIp(null);
        AuditContext.setUserAgent(null);

        // Verify values are removed
        assertNull(AuditContext.getUserId());
        assertNull(AuditContext.getSessionId());
        assertNull(AuditContext.getCorrelationId());
        assertNull(AuditContext.getApplication());
        assertNull(AuditContext.getComponent());
        assertNull(AuditContext.getSourceIp());
        assertNull(AuditContext.getUserAgent());
    }

    @Test
    public void testGetContextMapWithAllValues() {
        String userId = "user123";
        String sessionId = "sess456";
        String correlationId = "corr789";
        String application = "webapp";
        String component = "auth";
        String sourceIp = "192.168.1.1";
        String userAgent = "Mozilla/5.0";

        AuditContext.setUserId(userId);
        AuditContext.setSessionId(sessionId);
        AuditContext.setCorrelationId(correlationId);
        AuditContext.setApplication(application);
        AuditContext.setComponent(component);
        AuditContext.setSourceIp(sourceIp);
        AuditContext.setUserAgent(userAgent);

        Map<String, String> contextMap = AuditContext.getContextMap();

        assertEquals(7, contextMap.size());
        assertEquals(userId, contextMap.get("userId"));
        assertEquals(sessionId, contextMap.get("sessionId"));
        assertEquals(correlationId, contextMap.get("correlationId"));
        assertEquals(application, contextMap.get("application"));
        assertEquals(component, contextMap.get("component"));
        assertEquals(sourceIp, contextMap.get("sourceIp"));
        assertEquals(userAgent, contextMap.get("userAgent"));
    }

    @Test
    public void testGetContextMapWithPartialValues() {
        String userId = "user123";
        String application = "webapp";
        String sourceIp = "192.168.1.1";

        AuditContext.setUserId(userId);
        AuditContext.setApplication(application);
        AuditContext.setSourceIp(sourceIp);

        Map<String, String> contextMap = AuditContext.getContextMap();

        assertEquals(3, contextMap.size());
        assertEquals(userId, contextMap.get("userId"));
        assertEquals(application, contextMap.get("application"));
        assertEquals(sourceIp, contextMap.get("sourceIp"));
        assertNull(contextMap.get("sessionId"));
        assertNull(contextMap.get("correlationId"));
        assertNull(contextMap.get("component"));
        assertNull(contextMap.get("userAgent"));
    }

    @Test
    public void testGetContextMapWithNoValues() {
        Map<String, String> contextMap = AuditContext.getContextMap();
        assertEquals(0, contextMap.size());
    }

    @Test
    public void testFromContextWithAllValues() {
        String userId = "user123";
        String sessionId = "sess456";
        String correlationId = "corr789";
        String application = "webapp";
        String component = "auth";
        String sourceIp = "192.168.1.1";
        String userAgent = "Mozilla/5.0";

        AuditContext.setUserId(userId);
        AuditContext.setSessionId(sessionId);
        AuditContext.setCorrelationId(correlationId);
        AuditContext.setApplication(application);
        AuditContext.setComponent(component);
        AuditContext.setSourceIp(sourceIp);
        AuditContext.setUserAgent(userAgent);

        String eventType = "USER_LOGIN";
        String action = "login";
        String resource = "/api/login";
        AuditResult result = AuditResult.SUCCESS;
        String message = "User logged in successfully";
        Map<String, Object> details = Map.of("ip", "192.168.1.1");

        AuditEvent event = AuditContext.fromContext(eventType, action, resource, result, message, details);

        assertNotNull(event.timestamp());
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
    public void testFromContextWithPartialValues() {
        String userId = "user123";
        String application = "webapp";

        AuditContext.setUserId(userId);
        AuditContext.setApplication(application);

        String eventType = "USER_LOGIN";
        String action = "login";
        String resource = "/api/login";
        AuditResult result = AuditResult.SUCCESS;
        String message = "User logged in successfully";
        Map<String, Object> details = Map.of("ip", "192.168.1.1");

        AuditEvent event = AuditContext.fromContext(eventType, action, resource, result, message, details);

        assertEquals(userId, event.user_id());
        assertEquals(application, event.application());
        assertNull(event.session_id());
        assertNull(event.correlation_id());
        assertNull(event.component());
        assertNull(event.source_ip());
        assertNull(event.user_agent());
    }

    @Test
    public void testFromContextWithNullDetails() {
        AuditContext.setUserId("user123");

        AuditEvent event = AuditContext.fromContext("TEST", "action", "/resource", AuditResult.SUCCESS, "message", null);

        assertNull(event.details());
    }

    @Test
    public void testClear() {
        // Set all values
        AuditContext.setUserId("user123");
        AuditContext.setSessionId("sess456");
        AuditContext.setCorrelationId("corr789");
        AuditContext.setApplication("webapp");
        AuditContext.setComponent("auth");
        AuditContext.setSourceIp("192.168.1.1");
        AuditContext.setUserAgent("Mozilla/5.0");

        // Verify values are set
        assertNotNull(AuditContext.getUserId());
        assertNotNull(AuditContext.getSessionId());
        assertNotNull(AuditContext.getCorrelationId());
        assertNotNull(AuditContext.getApplication());
        assertNotNull(AuditContext.getComponent());
        assertNotNull(AuditContext.getSourceIp());
        assertNotNull(AuditContext.getUserAgent());

        // Clear context
        AuditContext.clear();

        // Verify all values are cleared
        assertNull(AuditContext.getUserId());
        assertNull(AuditContext.getSessionId());
        assertNull(AuditContext.getCorrelationId());
        assertNull(AuditContext.getApplication());
        assertNull(AuditContext.getComponent());
        assertNull(AuditContext.getSourceIp());
        assertNull(AuditContext.getUserAgent());
    }

    @Test
    public void testThreadLocalIsolation() throws InterruptedException {
        String userId1 = "user1";
        String userId2 = "user2";

        // Set value in main thread
        AuditContext.setUserId(userId1);
        assertEquals(userId1, AuditContext.getUserId());

        // Create and start another thread
        Thread thread = new Thread(() -> {
            // Set value in this thread
            AuditContext.setUserId(userId2);
            assertEquals(userId2, AuditContext.getUserId());
            
            // Clear context in this thread
            AuditContext.clear();
            assertNull(AuditContext.getUserId());
        });

        thread.start();
        thread.join();

        // Main thread should still have its original value
        assertEquals(userId1, AuditContext.getUserId());
    }

    @Test
    public void testMdcKeyConstants() {
        assertEquals("audit.user.id", AuditContext.USER_ID_KEY);
        assertEquals("audit.session.id", AuditContext.SESSION_ID_KEY);
        assertEquals("audit.correlation.id", AuditContext.CORRELATION_ID_KEY);
        assertEquals("audit.application", AuditContext.APPLICATION_KEY);
        assertEquals("audit.component", AuditContext.COMPONENT_KEY);
        assertEquals("audit.source.ip", AuditContext.SOURCE_IP_KEY);
        assertEquals("audit.user.agent", AuditContext.USER_AGENT_KEY);
    }

    @Test
    public void testDirectMdcAccess() {
        String userId = "user123";
        MDC.put(AuditContext.USER_ID_KEY, userId);
        assertEquals(userId, AuditContext.getUserId());

        String sessionId = "sess456";
        MDC.put(AuditContext.SESSION_ID_KEY, sessionId);
        assertEquals(sessionId, AuditContext.getSessionId());
    }
} 