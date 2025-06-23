package com.enterprise.audit.logging.config;

import org.junit.Test;

import java.io.File;
import java.util.Properties;

import static org.junit.Assert.*;

public class AuditConfigurationTest {

    @Test
    public void testDefaultConstructor() {
        AuditConfiguration config = new AuditConfiguration();

        assertEquals(AuditConfiguration.DEFAULT_LOG_DIRECTORY, config.getLogDirectory());
        assertEquals(AuditConfiguration.DEFAULT_LOG_FILE_PREFIX, config.getLogFilePrefix());
        assertEquals(AuditConfiguration.DEFAULT_LOG_FILE_EXTENSION, config.getLogFileExtension());
        assertEquals(AuditConfiguration.DEFAULT_AUTO_CREATE_DIRECTORY, config.isAutoCreateDirectory());
        assertEquals(AuditConfiguration.DEFAULT_MAX_FILE_SIZE_MB, config.getMaxFileSizeMB());
        assertEquals(AuditConfiguration.DEFAULT_MAX_FILES, config.getMaxFiles());
        assertEquals(AuditConfiguration.DEFAULT_APPEND_MODE, config.isAppendMode());
        assertEquals(AuditConfiguration.DEFAULT_STREAM_HOST, config.getStreamHost());
        assertEquals(AuditConfiguration.DEFAULT_STREAM_PORT, config.getStreamPort());
        assertEquals(AuditConfiguration.DEFAULT_STREAM_PROTOCOL, config.getStreamProtocol());
        assertEquals(AuditConfiguration.DEFAULT_STREAM_BUFFER_SIZE, config.getStreamBufferSize());
        assertEquals(AuditConfiguration.DEFAULT_STREAM_RECONNECT_MS, config.getStreamReconnectMs());
    }

    @Test
    public void testConstructorWithNullProperties() {
        AuditConfiguration config = new AuditConfiguration(null);

        // Should use default values when properties is null
        assertEquals(AuditConfiguration.DEFAULT_LOG_DIRECTORY, config.getLogDirectory());
        assertEquals(AuditConfiguration.DEFAULT_LOG_FILE_PREFIX, config.getLogFilePrefix());
        assertEquals(AuditConfiguration.DEFAULT_LOG_FILE_EXTENSION, config.getLogFileExtension());
    }

    @Test
    public void testConstructorWithEmptyProperties() {
        Properties properties = new Properties();
        AuditConfiguration config = new AuditConfiguration(properties);

        // Should use default values when properties is empty
        assertEquals(AuditConfiguration.DEFAULT_LOG_DIRECTORY, config.getLogDirectory());
        assertEquals(AuditConfiguration.DEFAULT_LOG_FILE_PREFIX, config.getLogFilePrefix());
        assertEquals(AuditConfiguration.DEFAULT_LOG_FILE_EXTENSION, config.getLogFileExtension());
    }

    @Test
    public void testLoadFromProperties() {
        Properties properties = new Properties();
        properties.setProperty(AuditConfiguration.LOG_DIRECTORY_KEY, "/custom/logs");
        properties.setProperty(AuditConfiguration.LOG_FILE_PREFIX_KEY, "custom-audit");
        properties.setProperty(AuditConfiguration.LOG_FILE_EXTENSION_KEY, ".json");
        properties.setProperty(AuditConfiguration.AUTO_CREATE_DIRECTORY_KEY, "false");
        properties.setProperty(AuditConfiguration.MAX_FILE_SIZE_MB_KEY, "200");
        properties.setProperty(AuditConfiguration.MAX_FILES_KEY, "20");
        properties.setProperty(AuditConfiguration.APPEND_MODE_KEY, "false");
        properties.setProperty(AuditConfiguration.STREAM_HOST_KEY, "custom-host");
        properties.setProperty(AuditConfiguration.STREAM_PORT_KEY, "8080");
        properties.setProperty(AuditConfiguration.STREAM_PROTOCOL_KEY, "UDP");
        properties.setProperty(AuditConfiguration.STREAM_BUFFER_SIZE_KEY, "16384");
        properties.setProperty(AuditConfiguration.STREAM_RECONNECT_MS_KEY, "5000");

        AuditConfiguration config = new AuditConfiguration(properties);

        assertEquals("/custom/logs", config.getLogDirectory());
        assertEquals("custom-audit", config.getLogFilePrefix());
        assertEquals(".json", config.getLogFileExtension());
        assertFalse(config.isAutoCreateDirectory());
        assertEquals(200L, config.getMaxFileSizeMB());
        assertEquals(20, config.getMaxFiles());
        assertFalse(config.isAppendMode());
        assertEquals("custom-host", config.getStreamHost());
        assertEquals(8080, config.getStreamPort());
        assertEquals("UDP", config.getStreamProtocol());
        assertEquals(16384, config.getStreamBufferSize());
        assertEquals(5000, config.getStreamReconnectMs());
    }

    @Test
    public void testLoadFromPropertiesWithInvalidNumbers() {
        Properties properties = new Properties();
        properties.setProperty(AuditConfiguration.MAX_FILE_SIZE_MB_KEY, "invalid");
        properties.setProperty(AuditConfiguration.MAX_FILES_KEY, "not-a-number");
        properties.setProperty(AuditConfiguration.STREAM_PORT_KEY, "invalid-port");
        properties.setProperty(AuditConfiguration.STREAM_BUFFER_SIZE_KEY, "invalid-buffer");
        properties.setProperty(AuditConfiguration.STREAM_RECONNECT_MS_KEY, "invalid-reconnect");

        AuditConfiguration config = new AuditConfiguration(properties);

        // Should use default values when parsing fails
        assertEquals(AuditConfiguration.DEFAULT_MAX_FILE_SIZE_MB, config.getMaxFileSizeMB());
        assertEquals(AuditConfiguration.DEFAULT_MAX_FILES, config.getMaxFiles());
        assertEquals(AuditConfiguration.DEFAULT_STREAM_PORT, config.getStreamPort());
        assertEquals(AuditConfiguration.DEFAULT_STREAM_BUFFER_SIZE, config.getStreamBufferSize());
        assertEquals(AuditConfiguration.DEFAULT_STREAM_RECONNECT_MS, config.getStreamReconnectMs());
    }

    @Test
    public void testLoadFromPropertiesWithNullProperties() {
        AuditConfiguration config = new AuditConfiguration();
        config.loadFromProperties(null);

        // Should not change values when properties is null
        assertEquals(AuditConfiguration.DEFAULT_LOG_DIRECTORY, config.getLogDirectory());
        assertEquals(AuditConfiguration.DEFAULT_LOG_FILE_PREFIX, config.getLogFilePrefix());
        assertEquals(AuditConfiguration.DEFAULT_LOG_FILE_EXTENSION, config.getLogFileExtension());
    }

    @Test
    public void testGetLogFilePath() {
        AuditConfiguration config = new AuditConfiguration();
        config.setLogDirectory("/custom/logs");
        config.setLogFilePrefix("test-audit");
        config.setLogFileExtension(".log");

        String expectedPath = "/custom/logs" + File.separator + "test-audit.log";
        assertEquals(expectedPath, config.getLogFilePath());
    }

    @Test
    public void testGettersAndSetters() {
        AuditConfiguration config = new AuditConfiguration();

        // Test file system configuration
        config.setLogDirectory("/test/logs");
        config.setLogFilePrefix("test-prefix");
        config.setLogFileExtension(".test");
        config.setAutoCreateDirectory(false);
        config.setMaxFileSizeMB(500L);
        config.setMaxFiles(50);
        config.setAppendMode(false);

        assertEquals("/test/logs", config.getLogDirectory());
        assertEquals("test-prefix", config.getLogFilePrefix());
        assertEquals(".test", config.getLogFileExtension());
        assertFalse(config.isAutoCreateDirectory());
        assertEquals(500L, config.getMaxFileSizeMB());
        assertEquals(50, config.getMaxFiles());
        assertFalse(config.isAppendMode());

        // Test streaming configuration
        config.setStreamHost("test-host");
        config.setStreamPort(9090);
        config.setStreamProtocol("TEST");
        config.setStreamBufferSize(32768);
        config.setStreamReconnectMs(10000);

        assertEquals("test-host", config.getStreamHost());
        assertEquals(9090, config.getStreamPort());
        assertEquals("TEST", config.getStreamProtocol());
        assertEquals(32768, config.getStreamBufferSize());
        assertEquals(10000, config.getStreamReconnectMs());
    }

    @Test
    public void testToString() {
        AuditConfiguration config = new AuditConfiguration();
        config.setLogDirectory("/test/logs");
        config.setStreamHost("test-host");
        config.setStreamPort(9090);

        String toString = config.toString();

        assertTrue("toString should contain log directory", toString.contains("/test/logs"));
        assertTrue("toString should contain stream host", toString.contains("test-host"));
        assertTrue("toString should contain stream port", toString.contains("9090"));
        assertTrue("toString should contain class name", toString.contains("AuditConfiguration"));
    }

    @Test
    public void testDefaultConstants() {
        assertEquals("./audit-logs", AuditConfiguration.DEFAULT_LOG_DIRECTORY);
        assertEquals("audit", AuditConfiguration.DEFAULT_LOG_FILE_PREFIX);
        assertEquals(".log", AuditConfiguration.DEFAULT_LOG_FILE_EXTENSION);
        assertTrue(AuditConfiguration.DEFAULT_AUTO_CREATE_DIRECTORY);
        assertEquals(100L, AuditConfiguration.DEFAULT_MAX_FILE_SIZE_MB);
        assertEquals(10, AuditConfiguration.DEFAULT_MAX_FILES);
        assertTrue(AuditConfiguration.DEFAULT_APPEND_MODE);
        assertEquals("localhost", AuditConfiguration.DEFAULT_STREAM_HOST);
        assertEquals(5044, AuditConfiguration.DEFAULT_STREAM_PORT);
        assertEquals("TCP", AuditConfiguration.DEFAULT_STREAM_PROTOCOL);
        assertEquals(8192, AuditConfiguration.DEFAULT_STREAM_BUFFER_SIZE);
        assertEquals(2000, AuditConfiguration.DEFAULT_STREAM_RECONNECT_MS);
    }

    @Test
    public void testConfigurationKeys() {
        assertEquals("audit.log.directory", AuditConfiguration.LOG_DIRECTORY_KEY);
        assertEquals("audit.log.file.prefix", AuditConfiguration.LOG_FILE_PREFIX_KEY);
        assertEquals("audit.log.file.extension", AuditConfiguration.LOG_FILE_EXTENSION_KEY);
        assertEquals("audit.log.auto.create.directory", AuditConfiguration.AUTO_CREATE_DIRECTORY_KEY);
        assertEquals("audit.log.max.file.size.mb", AuditConfiguration.MAX_FILE_SIZE_MB_KEY);
        assertEquals("audit.log.max.files", AuditConfiguration.MAX_FILES_KEY);
        assertEquals("audit.log.append.mode", AuditConfiguration.APPEND_MODE_KEY);
        assertEquals("audit.stream.host", AuditConfiguration.STREAM_HOST_KEY);
        assertEquals("audit.stream.port", AuditConfiguration.STREAM_PORT_KEY);
        assertEquals("audit.stream.protocol", AuditConfiguration.STREAM_PROTOCOL_KEY);
        assertEquals("audit.stream.buffer.size", AuditConfiguration.STREAM_BUFFER_SIZE_KEY);
        assertEquals("audit.stream.reconnect.ms", AuditConfiguration.STREAM_RECONNECT_MS_KEY);
    }

    @Test
    public void testBooleanPropertiesParsing() {
        Properties properties = new Properties();
        properties.setProperty(AuditConfiguration.AUTO_CREATE_DIRECTORY_KEY, "true");
        properties.setProperty(AuditConfiguration.APPEND_MODE_KEY, "false");

        AuditConfiguration config = new AuditConfiguration(properties);

        assertTrue(config.isAutoCreateDirectory());
        assertFalse(config.isAppendMode());
    }

    @Test
    public void testBooleanPropertiesWithInvalidValues() {
        Properties properties = new Properties();
        properties.setProperty(AuditConfiguration.AUTO_CREATE_DIRECTORY_KEY, "invalid");
        properties.setProperty(AuditConfiguration.APPEND_MODE_KEY, "not-boolean");

        AuditConfiguration config = new AuditConfiguration(properties);

        // Boolean.parseBoolean returns false for invalid values
        assertFalse(config.isAutoCreateDirectory());
        assertFalse(config.isAppendMode());
    }

    @Test
    public void testNegativeNumbers() {
        Properties properties = new Properties();
        properties.setProperty(AuditConfiguration.MAX_FILE_SIZE_MB_KEY, "-100");
        properties.setProperty(AuditConfiguration.MAX_FILES_KEY, "-5");
        properties.setProperty(AuditConfiguration.STREAM_PORT_KEY, "-8080");
        properties.setProperty(AuditConfiguration.STREAM_BUFFER_SIZE_KEY, "-1024");
        properties.setProperty(AuditConfiguration.STREAM_RECONNECT_MS_KEY, "-1000");

        AuditConfiguration config = new AuditConfiguration(properties);

        // Should accept negative numbers (validation should be done elsewhere)
        assertEquals(-100L, config.getMaxFileSizeMB());
        assertEquals(-5, config.getMaxFiles());
        assertEquals(-8080, config.getStreamPort());
        assertEquals(-1024, config.getStreamBufferSize());
        assertEquals(-1000, config.getStreamReconnectMs());
    }

    @Test
    public void testZeroValues() {
        Properties properties = new Properties();
        properties.setProperty(AuditConfiguration.MAX_FILE_SIZE_MB_KEY, "0");
        properties.setProperty(AuditConfiguration.MAX_FILES_KEY, "0");
        properties.setProperty(AuditConfiguration.STREAM_PORT_KEY, "0");
        properties.setProperty(AuditConfiguration.STREAM_BUFFER_SIZE_KEY, "0");
        properties.setProperty(AuditConfiguration.STREAM_RECONNECT_MS_KEY, "0");

        AuditConfiguration config = new AuditConfiguration(properties);

        assertEquals(0L, config.getMaxFileSizeMB());
        assertEquals(0, config.getMaxFiles());
        assertEquals(0, config.getStreamPort());
        assertEquals(0, config.getStreamBufferSize());
        assertEquals(0, config.getStreamReconnectMs());
    }
} 