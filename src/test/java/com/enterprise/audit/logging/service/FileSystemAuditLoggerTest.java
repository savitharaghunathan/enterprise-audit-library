package com.enterprise.audit.logging.service;

import com.enterprise.audit.logging.config.AuditConfiguration;
import com.enterprise.audit.logging.exception.AuditLoggingException;
import com.enterprise.audit.logging.model.AuditContext;
import com.enterprise.audit.logging.model.AuditEvent;
import com.enterprise.audit.logging.model.AuditResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class FileSystemAuditLoggerTest {

    private static final String TEST_LOG_DIR = "./test-audit-logs";
    private FileSystemAuditLogger auditLogger;
    private AuditConfiguration configuration;

    @Before
    public void setUp() throws AuditLoggingException {
        // Clean up any existing test directory
        cleanupTestDirectory();
        
        // Create test configuration
        configuration = new AuditConfiguration();
        configuration.setLogDirectory(TEST_LOG_DIR);
        configuration.setAutoCreateDirectory(true);
        
        // Create audit logger
        auditLogger = new FileSystemAuditLogger(configuration);
    }

    @After
    public void tearDown() throws AuditLoggingException {
        if (auditLogger != null) {
            auditLogger.close();
        }
        cleanupTestDirectory();
        AuditContext.clear();
    }

    @Test
    public void testLoggerInitialization() {
        assertTrue("Logger should be ready after initialization", auditLogger.isReady());
        assertNotNull("Configuration should not be null", auditLogger.getConfiguration());
        assertEquals("Log directory should match configuration", 
                    TEST_LOG_DIR, auditLogger.getConfiguration().getLogDirectory());
    }

    @Test
    public void testLogEventWithBuilder() throws AuditLoggingException, IOException {
        // Create an audit event using the builder pattern
        AuditEvent event = AuditEvent.builder()
                .eventType("USER_ACTION")
                .userId("user123")
                .sessionId("session456")
                .application("TestApp")
                .component("TestComponent")
                .action("LOGIN")
                .resource("/login")
                .result(AuditResult.SUCCESS)
                .message("User logged in successfully")
                .correlationId("corr789")
                .sourceIp("192.168.1.100")
                .userAgent("Mozilla/5.0")
                .build();

        // Log the event
        auditLogger.logEvent(event);

        // Verify the log file was created and contains the event
        verifyLogFileExists();
        List<String> logLines = readLogFileLines();
        assertEquals("Should have exactly one log entry", 1, logLines.size());
        
        String logLine = logLines.get(0);
        assertTrue("Log should contain event type", logLine.contains("USER_ACTION"));
        assertTrue("Log should contain user ID", logLine.contains("user123"));
        assertTrue("Log should contain result", logLine.contains("success"));
    }

    @Test
    public void testLogEventWithContext() throws AuditLoggingException, IOException {
        // Set up audit context
        AuditContext.setUserId("contextUser");
        AuditContext.setSessionId("contextSession");
        AuditContext.setApplication("ContextApp");
        AuditContext.setCorrelationId("contextCorr");

        // Log event using simple method
        auditLogger.logSuccess("CONTEXT_TEST", "EXECUTE", "/test", "Context test successful");

        // Verify the log file contains context information
        verifyLogFileExists();
        List<String> logLines = readLogFileLines();
        assertEquals("Should have exactly one log entry", 1, logLines.size());
        
        String logLine = logLines.get(0);
        assertTrue("Log should contain context user ID", logLine.contains("contextUser"));
        assertTrue("Log should contain context session ID", logLine.contains("contextSession"));
        assertTrue("Log should contain context application", logLine.contains("ContextApp"));
        assertTrue("Log should contain success result", logLine.contains("success"));
    }

    @Test
    public void testLogEventWithDetails() throws AuditLoggingException, IOException {
        Map<String, Object> details = new HashMap<>();
        details.put("ip_address", "10.0.0.1");
        details.put("user_agent", "TestAgent/1.0");
        details.put("request_id", "req123");
        details.put("duration_ms", 150);

        auditLogger.logEvent("API_CALL", "GET", "/api/users", 
                           AuditResult.SUCCESS, "API call successful", details);

        verifyLogFileExists();
        List<String> logLines = readLogFileLines();
        assertEquals("Should have exactly one log entry", 1, logLines.size());
        
        String logLine = logLines.get(0);
        assertTrue("Log should contain details", logLine.contains("\"details\""));
        assertTrue("Log should contain IP address", logLine.contains("10.0.0.1"));
        assertTrue("Log should contain duration", logLine.contains("150"));
    }

    @Test
    public void testLogFailure() throws AuditLoggingException, IOException {
        auditLogger.logFailure("DATABASE", "SELECT", "users_table", "Connection timeout");

        verifyLogFileExists();
        List<String> logLines = readLogFileLines();
        assertEquals("Should have exactly one log entry", 1, logLines.size());
        
        String logLine = logLines.get(0);
        assertTrue("Log should contain failure result", logLine.contains("failure"));
        assertTrue("Log should contain database event type", logLine.contains("DATABASE"));
        assertTrue("Log should contain timeout message", logLine.contains("Connection timeout"));
    }

    @Test
    public void testLogDenied() throws AuditLoggingException, IOException {
        auditLogger.logDenied("SECURITY", "ACCESS", "/admin", "Insufficient permissions");

        verifyLogFileExists();
        List<String> logLines = readLogFileLines();
        assertEquals("Should have exactly one log entry", 1, logLines.size());
        
        String logLine = logLines.get(0);
        assertTrue("Log should contain denied result", logLine.contains("denied"));
        assertTrue("Log should contain security event type", logLine.contains("SECURITY"));
        assertTrue("Log should contain permissions message", logLine.contains("Insufficient permissions"));
    }

    @Test
    public void testMultipleLogEntries() throws AuditLoggingException, IOException {
        // Log multiple events
        auditLogger.logSuccess("EVENT1", "ACTION1", "resource1", "Message 1");
        auditLogger.logFailure("EVENT2", "ACTION2", "resource2", "Message 2");
        auditLogger.logDenied("EVENT3", "ACTION3", "resource3", "Message 3");

        verifyLogFileExists();
        List<String> logLines = readLogFileLines();
        assertEquals("Should have exactly three log entries", 3, logLines.size());
        
        assertTrue("First line should contain EVENT1", logLines.get(0).contains("EVENT1"));
        assertTrue("Second line should contain EVENT2", logLines.get(1).contains("EVENT2"));
        assertTrue("Third line should contain EVENT3", logLines.get(2).contains("EVENT3"));
    }

    @Test(expected = AuditLoggingException.class)
    public void testLogAfterClose() throws AuditLoggingException {
        auditLogger.close();
        assertFalse("Logger should not be ready after close", auditLogger.isReady());
        
        // This should throw an exception
        auditLogger.logSuccess("TEST", "ACTION", "resource", "Should fail");
    }

    @Test(expected = AuditLoggingException.class)
    public void testLogNullEvent() throws AuditLoggingException {
        auditLogger.logEvent((AuditEvent) null);
    }

    private void verifyLogFileExists() {
        File logFile = new File(configuration.getLogFilePath());
        assertTrue("Log file should exist", logFile.exists());
        assertTrue("Log file should be a file", logFile.isFile());
        assertTrue("Log file should be readable", logFile.canRead());
    }

    private List<String> readLogFileLines() throws IOException {
        Path logFilePath = Paths.get(configuration.getLogFilePath());
        return Files.readAllLines(logFilePath);
    }

    private void cleanupTestDirectory() {
        File testDir = new File(TEST_LOG_DIR);
        if (testDir.exists()) {
            File[] files = testDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
            testDir.delete();
        }
    }
} 