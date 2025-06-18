package com.enterprise.audit.logging.config;

import java.io.File;
import java.util.Properties;

/**
 * Configuration class for audit logging settings.
 * This class provides configuration options for different audit logging backends.
 */
public class AuditConfiguration {
    
    // Default configuration values
    public static final String DEFAULT_LOG_DIRECTORY = "./audit-logs";
    public static final String DEFAULT_LOG_FILE_PREFIX = "audit";
    public static final String DEFAULT_LOG_FILE_EXTENSION = ".log";
    public static final boolean DEFAULT_AUTO_CREATE_DIRECTORY = true;
    public static final long DEFAULT_MAX_FILE_SIZE_MB = 100L;
    public static final int DEFAULT_MAX_FILES = 10;
    public static final boolean DEFAULT_APPEND_MODE = true;

    // Configuration keys
    public static final String LOG_DIRECTORY_KEY = "audit.log.directory";
    public static final String LOG_FILE_PREFIX_KEY = "audit.log.file.prefix";
    public static final String LOG_FILE_EXTENSION_KEY = "audit.log.file.extension";
    public static final String AUTO_CREATE_DIRECTORY_KEY = "audit.log.auto.create.directory";
    public static final String MAX_FILE_SIZE_MB_KEY = "audit.log.max.file.size.mb";
    public static final String MAX_FILES_KEY = "audit.log.max.files";
    public static final String APPEND_MODE_KEY = "audit.log.append.mode";

    private String logDirectory;
    private String logFilePrefix;
    private String logFileExtension;
    private boolean autoCreateDirectory;
    private long maxFileSizeMB;
    private int maxFiles;
    private boolean appendMode;

    /**
     * Creates a new audit configuration with default values.
     */
    public AuditConfiguration() {
        this.logDirectory = DEFAULT_LOG_DIRECTORY;
        this.logFilePrefix = DEFAULT_LOG_FILE_PREFIX;
        this.logFileExtension = DEFAULT_LOG_FILE_EXTENSION;
        this.autoCreateDirectory = DEFAULT_AUTO_CREATE_DIRECTORY;
        this.maxFileSizeMB = DEFAULT_MAX_FILE_SIZE_MB;
        this.maxFiles = DEFAULT_MAX_FILES;
        this.appendMode = DEFAULT_APPEND_MODE;
    }

    /**
     * Creates a new audit configuration from the provided properties.
     * 
     * @param properties the properties to load configuration from
     */
    public AuditConfiguration(Properties properties) {
        this();
        loadFromProperties(properties);
    }

    /**
     * Loads configuration values from the provided properties.
     * 
     * @param properties the properties to load from
     */
    public void loadFromProperties(Properties properties) {
        if (properties == null) {
            return;
        }

        this.logDirectory = properties.getProperty(LOG_DIRECTORY_KEY, DEFAULT_LOG_DIRECTORY);
        this.logFilePrefix = properties.getProperty(LOG_FILE_PREFIX_KEY, DEFAULT_LOG_FILE_PREFIX);
        this.logFileExtension = properties.getProperty(LOG_FILE_EXTENSION_KEY, DEFAULT_LOG_FILE_EXTENSION);
        
        this.autoCreateDirectory = Boolean.parseBoolean(
            properties.getProperty(AUTO_CREATE_DIRECTORY_KEY, String.valueOf(DEFAULT_AUTO_CREATE_DIRECTORY)));
        
        this.appendMode = Boolean.parseBoolean(
            properties.getProperty(APPEND_MODE_KEY, String.valueOf(DEFAULT_APPEND_MODE)));

        try {
            this.maxFileSizeMB = Long.parseLong(
                properties.getProperty(MAX_FILE_SIZE_MB_KEY, String.valueOf(DEFAULT_MAX_FILE_SIZE_MB)));
        } catch (NumberFormatException e) {
            this.maxFileSizeMB = DEFAULT_MAX_FILE_SIZE_MB;
        }

        try {
            this.maxFiles = Integer.parseInt(
                properties.getProperty(MAX_FILES_KEY, String.valueOf(DEFAULT_MAX_FILES)));
        } catch (NumberFormatException e) {
            this.maxFiles = DEFAULT_MAX_FILES;
        }
    }

    /**
     * Gets the full path to the current log file.
     * 
     * @return the log file path
     */
    public String getLogFilePath() {
        return logDirectory + File.separator + logFilePrefix + logFileExtension;
    }

    // Getters and Setters

    public String getLogDirectory() {
        return logDirectory;
    }

    public void setLogDirectory(String logDirectory) {
        this.logDirectory = logDirectory;
    }

    public String getLogFilePrefix() {
        return logFilePrefix;
    }

    public void setLogFilePrefix(String logFilePrefix) {
        this.logFilePrefix = logFilePrefix;
    }

    public String getLogFileExtension() {
        return logFileExtension;
    }

    public void setLogFileExtension(String logFileExtension) {
        this.logFileExtension = logFileExtension;
    }

    public boolean isAutoCreateDirectory() {
        return autoCreateDirectory;
    }

    public void setAutoCreateDirectory(boolean autoCreateDirectory) {
        this.autoCreateDirectory = autoCreateDirectory;
    }

    public long getMaxFileSizeMB() {
        return maxFileSizeMB;
    }

    public void setMaxFileSizeMB(long maxFileSizeMB) {
        this.maxFileSizeMB = maxFileSizeMB;
    }

    public int getMaxFiles() {
        return maxFiles;
    }

    public void setMaxFiles(int maxFiles) {
        this.maxFiles = maxFiles;
    }

    public boolean isAppendMode() {
        return appendMode;
    }

    public void setAppendMode(boolean appendMode) {
        this.appendMode = appendMode;
    }

    @Override
    public String toString() {
        return "AuditConfiguration{" +
               "logDirectory='" + logDirectory + '\'' +
               ", logFilePrefix='" + logFilePrefix + '\'' +
               ", logFileExtension='" + logFileExtension + '\'' +
               ", autoCreateDirectory=" + autoCreateDirectory +
               ", maxFileSizeMB=" + maxFileSizeMB +
               ", maxFiles=" + maxFiles +
               ", appendMode=" + appendMode +
               '}';
    }
} 