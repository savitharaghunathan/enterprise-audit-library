package com.enterprise.audit.logging.service;

import com.enterprise.audit.logging.config.AuditConfiguration;
import com.enterprise.audit.logging.model.AuditEvent;
import com.enterprise.audit.logging.model.AuditResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.*;

import static org.junit.Assert.*;

public class StreamableAuditLoggerTest {
    private static final int MOCK_PORT = 5055;
    private static final String HOST = "localhost";
    private ExecutorService serverExecutor;
    private ServerSocket serverSocket;
    private BlockingQueue<String> receivedMessages;

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
    }

    @After
    public void tearDown() throws Exception {
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
        if (serverExecutor != null) {
            serverExecutor.shutdownNow();
        }
    }

    @Test
    public void testStreamableAuditLoggerSendsEvent() throws Exception {
        AuditConfiguration config = new AuditConfiguration();
        config.setStreamHost(HOST);
        config.setStreamPort(MOCK_PORT);
        AuditLogger logger = new StreamableAuditLogger(config);
        AuditEvent event = new AuditEvent(
                Instant.now(), "TEST_EVENT", "userX", "sessY", "AppZ", "CompA",
                "ACTION", "/resource", AuditResult.SUCCESS, "Test message",
                Map.of("foo", "bar"), "corrId", "127.0.0.1", "JUnit/1.0"
        );
        logger.logEventAsync(event).get(2, TimeUnit.SECONDS);
        String received = receivedMessages.poll(2, TimeUnit.SECONDS);
        assertNotNull("Should receive a message from the logger", received);
        assertTrue("Received JSON should contain event type", received.contains("TEST_EVENT"));
        assertTrue("Received JSON should contain user ID", received.contains("userX"));
        assertTrue("Received JSON should contain result", received.contains("success"));
        assertTrue("Received JSON should contain details", received.contains("foo"));
        logger.close();
    }

    @Test
    public void testLoggerIsReady() throws Exception {
        AuditConfiguration config = new AuditConfiguration();
        config.setStreamHost(HOST);
        config.setStreamPort(MOCK_PORT);
        AuditLogger logger = new StreamableAuditLogger(config);
        assertTrue("Logger should be ready when mock server is up", logger.isReady());
        logger.close();
    }

    @Test
    public void testLoggerNotReadyWhenServerDown() throws Exception {
        AuditConfiguration config = new AuditConfiguration();
        config.setStreamHost(HOST);
        config.setStreamPort(5999); // Unused port
        AuditLogger logger = new StreamableAuditLogger(config);
        assertFalse("Logger should not be ready when server is down", logger.isReady());
        logger.close();
    }
} 