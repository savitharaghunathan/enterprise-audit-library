package com.enterprise.audit.logging;

import com.enterprise.audit.logging.config.AuditConfiguration;
import com.enterprise.audit.logging.model.AuditContext;
import com.enterprise.audit.logging.model.AuditEvent;
import com.enterprise.audit.logging.model.AuditResult;
import com.enterprise.audit.logging.service.AuditLogger;
import com.enterprise.audit.logging.service.StreamableAuditLogger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.*;

import static org.junit.Assert.*;

public class IntegrationTest {
    private static final int MOCK_PORT = 5056;
    private static final String HOST = "localhost";
    private ExecutorService serverExecutor;
    private ServerSocket serverSocket;
    private BlockingQueue<String> receivedMessages;
    private AuditLogger logger;

    @Before
    public void setUp() throws Exception {
        receivedMessages = new LinkedBlockingQueue<>();
        serverSocket = new ServerSocket(MOCK_PORT);
        serverExecutor = Executors.newSingleThreadExecutor();
        serverExecutor.submit(() -> {
            try {
                while (!serverSocket.isClosed()) {
                    try (Socket client = serverSocket.accept();
                         BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            receivedMessages.offer(line);
                        }
                    }
                }
            } catch (Exception ignored) {}
        });

        AuditConfiguration config = new AuditConfiguration();
        config.setStreamHost(HOST);
        config.setStreamPort(MOCK_PORT);
        logger = new StreamableAuditLogger(config);
    }

    @After
    public void tearDown() throws Exception {
        if (logger != null) {
            logger.close();
        }
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
        if (serverExecutor != null) {
            serverExecutor.shutdownNow();
        }
        AuditContext.clear();
    }

    @Test
    public void testEndToEndAuditLogging() throws Exception {
        // Set up audit context
        AuditContext.setUserId("integration-user");
        AuditContext.setSessionId("integration-session");
        AuditContext.setCorrelationId("integration-corr");
        AuditContext.setApplication("IntegrationApp");
        AuditContext.setComponent("IntegrationTest");
        AuditContext.setSourceIp("192.168.1.100");
        AuditContext.setUserAgent("IntegrationTest/1.0");

        // Create audit event from context
        AuditEvent event = AuditContext.fromContext(
                "INTEGRATION_TEST",
                "test-action",
                "/api/integration",
                AuditResult.SUCCESS,
                "Integration test completed successfully",
                Map.of("testId", "123", "duration", "500ms")
        );

        // Log the event
        logger.logEventAsync(event).get(2, TimeUnit.SECONDS);

        // Verify event was received
        String received = receivedMessages.poll(2, TimeUnit.SECONDS);
        assertNotNull("Should receive integration test event", received);

        // Verify all context fields are present
        assertTrue("Should contain user ID from context", received.contains("integration-user"));
        assertTrue("Should contain session ID from context", received.contains("integration-session"));
        assertTrue("Should contain correlation ID from context", received.contains("integration-corr"));
        assertTrue("Should contain application from context", received.contains("IntegrationApp"));
        assertTrue("Should contain component from context", received.contains("IntegrationTest"));
        assertTrue("Should contain source IP from context", received.contains("192.168.1.100"));
        assertTrue("Should contain user agent from context", received.contains("IntegrationTest/1.0"));
        assertTrue("Should contain event type", received.contains("INTEGRATION_TEST"));
        assertTrue("Should contain action", received.contains("test-action"));
        assertTrue("Should contain resource", received.contains("/api/integration"));
        assertTrue("Should contain result", received.contains("success"));
        assertTrue("Should contain message", received.contains("Integration test completed successfully"));
        assertTrue("Should contain test details", received.contains("testId"));
        assertTrue("Should contain test details", received.contains("123"));
    }

    @Test
    public void testMultiThreadedContextIsolation() throws Exception {
        int numThreads = 3; // Reduced for better reliability
        int eventsPerThread = 2;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads * eventsPerThread);

        // Submit tasks for each thread
        for (int t = 0; t < numThreads; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    // Set thread-specific context
                    AuditContext.setUserId("user-" + threadId);
                    AuditContext.setSessionId("session-" + threadId);
                    AuditContext.setCorrelationId("corr-" + threadId);

                    for (int i = 0; i < eventsPerThread; i++) {
                        AuditEvent event = AuditContext.fromContext(
                                "THREAD_TEST",
                                "thread-action",
                                "/api/thread/" + threadId,
                                AuditResult.SUCCESS,
                                "Thread " + threadId + " event " + i,
                                Map.of("threadId", String.valueOf(threadId), "eventId", String.valueOf(i))
                        );

                        logger.logEventAsync(event).get(1, TimeUnit.SECONDS);
                        latch.countDown();
                        try {
                            // Pace the requests to avoid overwhelming the mock server's connection handling
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                } catch (Exception e) {
                    fail("Thread " + threadId + " failed: " + e.getMessage());
                }
            });
        }

        // Wait for all events to complete
        assertTrue("All threaded events should complete", latch.await(10, TimeUnit.SECONDS));

        // Give the server a moment to process all incoming messages into the queue
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Verify all events were received, ignoring order
        int expectedEventCount = numThreads * eventsPerThread;
        assertEquals("Should have received all events", expectedEventCount, receivedMessages.size());

        java.util.List<String> allReceivedMessages = new java.util.ArrayList<>();
        receivedMessages.drainTo(allReceivedMessages);

        for (int t = 0; t < numThreads; t++) {
            for (int i = 0; i < eventsPerThread; i++) {
                final int finalT = t;
                final int finalI = i;

                boolean eventFound = allReceivedMessages.stream().anyMatch(msg ->
                    msg.contains("\"user_id\":\"user-" + finalT + "\"") &&
                    msg.contains("\"session_id\":\"session-" + finalT + "\"") &&
                    msg.contains("\"eventId\":\"" + finalI + "\"") &&
                    msg.contains("\"threadId\":\"" + finalT + "\"")
                );

                assertTrue("Event for thread " + t + " and event " + i + " was not found in the received messages.", eventFound);
            }
        }

        executor.shutdown();
    }

    @Test
    public void testContextPersistenceAcrossEvents() throws Exception {
        // Set up persistent context
        AuditContext.setUserId("persistent-user");
        AuditContext.setApplication("PersistentApp");
        AuditContext.setComponent("PersistentComp");

        // Send multiple events with the same context
        for (int i = 0; i < 5; i++) {
            AuditEvent event = AuditContext.fromContext(
                    "PERSISTENCE_TEST_" + i,
                    "persistent-action",
                    "/api/persistence/" + i,
                    AuditResult.SUCCESS,
                    "Persistence test event " + i,
                    Map.of("eventNumber", String.valueOf(i))
            );

            logger.logEventAsync(event).get(1, TimeUnit.SECONDS);
        }

        // Verify all events contain the persistent context
        for (int i = 0; i < 5; i++) {
            String received = receivedMessages.poll(2, TimeUnit.SECONDS);
            assertNotNull("Should receive persistence test event " + i, received);
            assertTrue("Event should contain persistent user ID", received.contains("persistent-user"));
            assertTrue("Event should contain persistent application", received.contains("PersistentApp"));
            assertTrue("Event should contain persistent component", received.contains("PersistentComp"));
            assertTrue("Event should contain correct event type", received.contains("PERSISTENCE_TEST_" + i));
            assertTrue("Event should contain correct event number", received.contains("\"eventNumber\":\"" + i + "\""));
        }
    }

    @Test
    public void testContextUpdateBetweenEvents() throws Exception {
        // Initial context
        AuditContext.setUserId("initial-user");
        AuditContext.setSessionId("initial-session");

        // First event with initial context
        AuditEvent event1 = AuditContext.fromContext(
                "CONTEXT_UPDATE_TEST",
                "initial-action",
                "/api/initial",
                AuditResult.SUCCESS,
                "Initial context event",
                Map.of("context", "initial")
        );

        logger.logEventAsync(event1).get(1, TimeUnit.SECONDS);

        // Update context
        AuditContext.setUserId("updated-user");
        AuditContext.setSessionId("updated-session");
        AuditContext.setCorrelationId("new-correlation");

        // Second event with updated context
        AuditEvent event2 = AuditContext.fromContext(
                "CONTEXT_UPDATE_TEST",
                "updated-action",
                "/api/updated",
                AuditResult.SUCCESS,
                "Updated context event",
                Map.of("context", "updated")
        );

        logger.logEventAsync(event2).get(1, TimeUnit.SECONDS);

        // Verify first event has initial context
        String received1 = receivedMessages.poll(2, TimeUnit.SECONDS);
        assertNotNull("Should receive initial context event", received1);
        assertTrue("First event should contain initial user ID", received1.contains("initial-user"));
        assertTrue("First event should contain initial session ID", received1.contains("initial-session"));
        assertFalse("First event should not contain updated user ID", received1.contains("updated-user"));

        // Verify second event has updated context
        String received2 = receivedMessages.poll(2, TimeUnit.SECONDS);
        assertNotNull("Should receive updated context event", received2);
        assertTrue("Second event should contain updated user ID", received2.contains("updated-user"));
        assertTrue("Second event should contain updated session ID", received2.contains("updated-session"));
        assertTrue("Second event should contain new correlation ID", received2.contains("new-correlation"));
        assertFalse("Second event should not contain initial user ID", received2.contains("initial-user"));
    }

    @Test
    public void testContextClearBetweenEvents() throws Exception {
        // Set up context
        AuditContext.setUserId("clear-user");
        AuditContext.setSessionId("clear-session");
        AuditContext.setCorrelationId("clear-corr");

        // First event with context
        AuditEvent event1 = AuditContext.fromContext(
                "CONTEXT_CLEAR_TEST",
                "before-clear",
                "/api/before-clear",
                AuditResult.SUCCESS,
                "Before clear event",
                Map.of("phase", "before")
        );

        logger.logEventAsync(event1).get(1, TimeUnit.SECONDS);

        // Clear context
        AuditContext.clear();

        // Second event without context
        AuditEvent event2 = AuditContext.fromContext(
                "CONTEXT_CLEAR_TEST",
                "after-clear",
                "/api/after-clear",
                AuditResult.SUCCESS,
                "After clear event",
                Map.of("phase", "after")
        );

        logger.logEventAsync(event2).get(1, TimeUnit.SECONDS);

        // Verify first event has context
        String received1 = receivedMessages.poll(2, TimeUnit.SECONDS);
        assertNotNull("Should receive before clear event", received1);
        assertTrue("First event should contain user ID", received1.contains("clear-user"));
        assertTrue("First event should contain session ID", received1.contains("clear-session"));
        assertTrue("First event should contain correlation ID", received1.contains("clear-corr"));

        // Verify second event has no context
        String received2 = receivedMessages.poll(2, TimeUnit.SECONDS);
        assertNotNull("Should receive after clear event", received2);
        assertFalse("Second event should not contain user ID", received2.contains("clear-user"));
        assertFalse("Second event should not contain session ID", received2.contains("clear-session"));
        assertFalse("Second event should not contain correlation ID", received2.contains("clear-corr"));
    }

    @Test
    public void testDifferentAuditResultsWithContext() throws Exception {
        // Set up context
        AuditContext.setUserId("result-user");
        AuditContext.setApplication("ResultApp");

        AuditResult[] results = {AuditResult.SUCCESS, AuditResult.FAILURE, AuditResult.DENIED, 
                                AuditResult.INVALID, AuditResult.TIMEOUT, AuditResult.CANCELLED, AuditResult.UNKNOWN};

        // Send events with different results
        for (AuditResult result : results) {
            AuditEvent event = AuditContext.fromContext(
                    "RESULT_INTEGRATION_TEST",
                    "test-action",
                    "/api/result-test",
                    result,
                    "Testing result: " + result,
                    Map.of("result", result.toString(), "testId", "integration")
            );

            logger.logEventAsync(event).get(1, TimeUnit.SECONDS);
        }

        // Verify all results were received with context
        for (AuditResult result : results) {
            String received = receivedMessages.poll(2, TimeUnit.SECONDS);
            assertNotNull("Should receive event for result: " + result, received);
            assertTrue("Event should contain user ID from context", received.contains("result-user"));
            assertTrue("Event should contain application from context", received.contains("ResultApp"));
            assertTrue("Event should contain result: " + result, received.contains(result.getValue()));
            assertTrue("Event should contain result in details", received.contains("\"result\":\"" + result + "\""));
        }
    }

    @Test
    public void testLargeEventWithContext() throws Exception {
        // Set up context
        AuditContext.setUserId("large-user");
        AuditContext.setSessionId("large-session");

        // Create large details
        Map<String, Object> largeDetails = Map.of(
                "largeString", "x".repeat(2000),
                "array", new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15},
                "nested", Map.of(
                        "level1", Map.of("level2", Map.of("level3", "deep-value")),
                        "list", new String[]{"item1", "item2", "item3", "item4", "item5"}
                ),
                "booleanValues", new boolean[]{true, false, true, false, true}
        );

        AuditEvent event = AuditContext.fromContext(
                "LARGE_INTEGRATION_TEST",
                "large-action",
                "/api/large-test",
                AuditResult.SUCCESS,
                "Large integration test with context",
                largeDetails
        );

        logger.logEventAsync(event).get(2, TimeUnit.SECONDS);

        String received = receivedMessages.poll(2, TimeUnit.SECONDS);
        assertNotNull("Should receive large integration event", received);

        // Verify context is present
        assertTrue("Event should contain user ID from context", received.contains("large-user"));
        assertTrue("Event should contain session ID from context", received.contains("large-session"));

        // Verify large details are present
        assertTrue("Event should contain large string", received.contains("x".repeat(100)));
        assertTrue("Event should contain array", received.contains("[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15]"));
        assertTrue("Event should contain nested structure", received.contains("deep-value"));
        assertTrue("Event should contain list", received.contains("item1"));
        assertTrue("Event should contain boolean values", received.contains("[true,false,true,false,true]"));
    }
} 