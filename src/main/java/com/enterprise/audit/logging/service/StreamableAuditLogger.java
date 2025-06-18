package com.enterprise.audit.logging.service;

import com.enterprise.audit.logging.config.AuditConfiguration;
import com.enterprise.audit.logging.model.AuditEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * StreamableAuditLogger streams audit events asynchronously over TCP using virtual threads (Java 21+).
 * Designed for cloud-native, high-throughput, low-latency environments.
 */
public class StreamableAuditLogger implements AuditLogger {
    private final AuditConfiguration config;
    private final ObjectMapper objectMapper;
    private final ExecutorService executor;
    private volatile boolean closed = false;

    public StreamableAuditLogger(AuditConfiguration config) {
        this.config = config;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        // Use a virtual thread per task executor
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
    }

    @Override
    public CompletableFuture<Void> logEventAsync(AuditEvent auditEvent) {
        return CompletableFuture.runAsync(() -> sendEvent(auditEvent), executor);
    }

    @Override
    public CompletableFuture<Void> logEventAsyncSafe(AuditEvent auditEvent) {
        return CompletableFuture.runAsync(() -> {
            try {
                sendEvent(auditEvent);
            } catch (Exception e) {
                throw new RuntimeException("Failed to stream audit event", e);
            }
        }, executor);
    }

    private void sendEvent(AuditEvent event) {
        if (closed) throw new IllegalStateException("Audit logger is closed");
        String json;
        try {
            json = objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize audit event", e);
        }
        // Stream over TCP
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(config.getStreamHost(), config.getStreamPort()), 2000);
            socket.setSendBufferSize(config.getStreamBufferSize());
            try (OutputStream out = socket.getOutputStream()) {
                out.write(json.getBytes());
                out.write('\n'); // Line-delimited JSON
                out.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to send audit event over TCP", e);
        }
    }

    @Override
    public boolean isReady() {
        // Optionally, try to connect to the host/port
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(config.getStreamHost(), config.getStreamPort()), 1000);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public void close() {
        closed = true;
        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {}
    }

    @Override
    @Deprecated
    public void logEvent(AuditEvent auditEvent) {
        logEventAsync(auditEvent).join();
    }
} 