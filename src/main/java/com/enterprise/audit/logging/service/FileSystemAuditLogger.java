package com.enterprise.audit.logging.service;

import com.enterprise.audit.logging.config.AuditConfiguration;
import com.enterprise.audit.logging.exception.AuditLoggingException;
import com.enterprise.audit.logging.model.AuditContext;
import com.enterprise.audit.logging.model.AuditEvent;
import com.enterprise.audit.logging.model.AuditResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implementation of AuditLogger that writes audit events to the local filesystem
 * in line-delimited JSON format. This implementation is designed for Java 8
 * and provides synchronous logging suitable for log-shipping agents like Filebeat.
 */
public class FileSystemAuditLogger implements AuditLogger {

    private static final Logger logger = LoggerFactory.getLogger(FileSystemAuditLogger.class);

    private final AuditConfiguration configuration;
    private final ObjectMapper objectMapper;
    private final ReentrantLock writeLock;
    private volatile boolean closed = false;

    /**
     * Creates a new FileSystemAuditLogger with default configuration.
     * 
     * @throws AuditLoggingException if the logger cannot be initialized
     */
    public FileSystemAuditLogger() throws AuditLoggingException {
        this(new AuditConfiguration());
    }

    /**
     * Creates a new FileSystemAuditLogger with the specified configuration.
     * 
     * @param configuration the audit configuration to use
     * @throws AuditLoggingException if the logger cannot be initialized
     */
    public FileSystemAuditLogger(AuditConfiguration configuration) throws AuditLoggingException {
        if (configuration == null) {
            throw new AuditLoggingException("Configuration cannot be null");
        }

        this.configuration = configuration;
        this.writeLock = new ReentrantLock();
        this.objectMapper = createObjectMapper();

        try {
            initializeLogDirectory();
        } catch (Exception e) {
            throw new AuditLoggingException("Failed to initialize audit logger", e);
        }

        logger.info("FileSystemAuditLogger initialized with configuration: {}", configuration);
    }

    /**
     * Creates and configures the Jackson ObjectMapper for JSON serialization.
     */
    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    /**
     * Initializes the log directory if it doesn't exist and auto-creation is enabled.
     */
    private void initializeLogDirectory() throws IOException {
        File logDir = new File(configuration.getLogDirectory());
        
        if (!logDir.exists()) {
            if (configuration.isAutoCreateDirectory()) {
                if (!logDir.mkdirs()) {
                    throw new IOException("Failed to create log directory: " + logDir.getAbsolutePath());
                }
                logger.info("Created audit log directory: {}", logDir.getAbsolutePath());
            } else {
                throw new IOException("Log directory does not exist and auto-creation is disabled: " 
                                    + logDir.getAbsolutePath());
            }
        }

        if (!logDir.isDirectory()) {
            throw new IOException("Log directory path is not a directory: " + logDir.getAbsolutePath());
        }

        if (!logDir.canWrite()) {
            throw new IOException("Cannot write to log directory: " + logDir.getAbsolutePath());
        }
    }

    @Override
    public void logEvent(AuditEvent auditEvent) throws AuditLoggingException {
        if (auditEvent == null) {
            throw new AuditLoggingException("Audit event cannot be null");
        }

        checkClosed();
        
        try {
            String jsonLine = objectMapper.writeValueAsString(auditEvent);
            writeToFile(jsonLine);
            
            logger.debug("Logged audit event: {}", auditEvent.getEventType());
        } catch (Exception e) {
            throw new AuditLoggingException("Failed to log audit event", e);
        }
    }

    @Override
    public void logEvent(String eventType, String action, String resource, 
                        AuditResult result, String message) throws AuditLoggingException {
        logEvent(eventType, action, resource, result, message, null);
    }

    @Override
    public void logEvent(String eventType, String action, String resource, 
                        AuditResult result, String message, Map<String, Object> details) 
                        throws AuditLoggingException {
        
        AuditEvent.Builder builder = AuditContext.populateBuilder(AuditEvent.builder())
                .eventType(eventType)
                .action(action)
                .resource(resource)
                .result(result)
                .message(message);
        
        if (details != null) {
            builder.details(details);
        }

        logEvent(builder.build());
    }

    @Override
    public void logSuccess(String eventType, String action, String resource, String message) 
                          throws AuditLoggingException {
        logEvent(eventType, action, resource, AuditResult.SUCCESS, message);
    }

    @Override
    public void logFailure(String eventType, String action, String resource, String message) 
                          throws AuditLoggingException {
        logEvent(eventType, action, resource, AuditResult.FAILURE, message);
    }

    @Override
    public void logDenied(String eventType, String action, String resource, String message) 
                         throws AuditLoggingException {
        logEvent(eventType, action, resource, AuditResult.DENIED, message);
    }

    @Override
    public boolean isReady() {
        if (closed) {
            return false;
        }

        try {
            File logDir = new File(configuration.getLogDirectory());
            return logDir.exists() && logDir.isDirectory() && logDir.canWrite();
        } catch (Exception e) {
            logger.warn("Failed to check audit logger readiness", e);
            return false;
        }
    }

    @Override
    public void close() throws AuditLoggingException {
        writeLock.lock();
        try {
            closed = true;
            logger.info("FileSystemAuditLogger closed");
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Writes a JSON line to the audit log file.
     */
    private void writeToFile(String jsonLine) throws IOException, AuditLoggingException {
        writeLock.lock();
        try {
            checkClosed();
            
            File logFile = new File(configuration.getLogFilePath());
            
            try (BufferedWriter writer = new BufferedWriter(
                    new FileWriter(logFile, configuration.isAppendMode()))) {
                writer.write(jsonLine);
                writer.newLine();
                writer.flush();
            }
            
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Checks if the logger has been closed and throws an exception if it has.
     */
    private void checkClosed() throws AuditLoggingException {
        if (closed) {
            throw new AuditLoggingException("Audit logger has been closed");
        }
    }

    /**
     * Gets the current configuration.
     */
    public AuditConfiguration getConfiguration() {
        return configuration;
    }
} 