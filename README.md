# Enterprise Audit Logging Library

A reusable Java 8 component designed to provide consistent and traceable logging in large-scale enterprise environments. This library offers a uniform way for applications to emit structured audit events, essential for compliance with regulations, security audits, and operational visibility.

## Features

- **Java 8 Compatible**: Built specifically for Java 8 environments
- **Structured JSON Logging**: Emits line-delimited JSON format suitable for log shipping agents like Filebeat
- **Thread-Safe**: Safe for use in multi-threaded applications
- **SLF4J MDC Integration**: Uses SLF4J's Mapped Diagnostic Context for thread-local context propagation
- **Flexible Configuration**: Supports configuration via Properties files or programmatic setup
- **Builder Pattern**: Easy event construction with fluent builder API
- **File-based Backend**: Version 1.0 supports filesystem-based logging with configurable directories
- **Comprehensive Test Coverage**: Includes unit tests and examples

## Quick Start

### 1. Add Dependency

Add the library to your project:

**Maven:**
```xml
<dependency>
    <groupId>com.enterprise</groupId>
    <artifactId>audit-logging-library</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Gradle:**
```groovy
implementation 'com.enterprise:audit-logging-library:1.0.0'
```

### 2. Basic Usage

```java
import com.enterprise.audit.logging.service.AuditLogger;
import com.enterprise.audit.logging.service.FileSystemAuditLogger;
import com.enterprise.audit.logging.exception.AuditLoggingException;

public class MyApplication {
    public static void main(String[] args) {
        try {
            AuditLogger auditLogger = new FileSystemAuditLogger();
            
            // Log a successful operation
            auditLogger.logSuccess("USER_ACTION", "LOGIN", "/login", "User logged in successfully");
            
            // Log a failure
            auditLogger.logFailure("DATABASE", "QUERY", "users_table", "Connection timeout");
            
            // Log an access denial
            auditLogger.logDenied("SECURITY", "ACCESS", "/admin", "Insufficient permissions");
            
            auditLogger.close();
        } catch (AuditLoggingException e) {
            System.err.println("Audit logging failed: " + e.getMessage());
        }
    }
}
```

## Configuration

### Default Configuration

By default, the library writes audit logs to:
- **Directory**: `./audit-logs`
- **File Name**: `audit.log`
- **Format**: Line-delimited JSON
- **Behavior**: Appends to existing files, creates directory if it doesn't exist

### Custom Configuration

#### Using Properties

```java
Properties props = new Properties();
props.setProperty("audit.log.directory", "/var/log/myapp");
props.setProperty("audit.log.file.prefix", "myapp-audit");
props.setProperty("audit.log.max.file.size.mb", "100");
props.setProperty("audit.log.auto.create.directory", "true");

AuditConfiguration config = new AuditConfiguration(props);
AuditLogger auditLogger = new FileSystemAuditLogger(config);
```

#### Configuration Properties

| Property | Default | Description |
|----------|---------|-------------|
| `audit.log.directory` | `./audit-logs` | Directory where audit logs are stored |
| `audit.log.file.prefix` | `audit` | Prefix for audit log files |
| `audit.log.file.extension` | `.log` | File extension for audit logs |
| `audit.log.auto.create.directory` | `true` | Automatically create log directory if it doesn't exist |
| `audit.log.max.file.size.mb` | `100` | Maximum file size in MB (for future use) |
| `audit.log.max.files` | `10` | Maximum number of log files to keep (for future use) |
| `audit.log.append.mode` | `true` | Append to existing files vs. overwrite |

## Using Audit Context

The library supports automatic context inclusion using SLF4J's MDC:

```java
import com.enterprise.audit.logging.model.AuditContext;

// Set context information (thread-local)
AuditContext.setUserId("john.doe");
AuditContext.setSessionId("session_12345");
AuditContext.setApplication("WebPortal");
AuditContext.setCorrelationId("req_67890");
AuditContext.setSourceIp("192.168.1.100");

// All subsequent audit events will include this context automatically
auditLogger.logSuccess("USER_ACTION", "VIEW_PROFILE", "/profile", "Profile accessed");

// Clear context when done
AuditContext.clear();
```

## Advanced Usage

### Builder Pattern for Complex Events

```java
import com.enterprise.audit.logging.model.AuditEvent;
import com.enterprise.audit.logging.model.AuditResult;

AuditEvent event = AuditEvent.builder()
    .eventType("API_CALL")
    .userId("api_user_123")
    .sessionId("api_session_456")
    .application("RestAPI")
    .component("OrderController")
    .action("CREATE_ORDER")
    .resource("/api/v1/orders")
    .result(AuditResult.SUCCESS)
    .message("Order created successfully")
    .correlationId("order_req_789")
    .sourceIp("10.0.0.15")
    .userAgent("ApiClient/1.0")
    .build();

auditLogger.logEvent(event);
```

### Events with Additional Details

```java
Map<String, Object> details = new HashMap<>();
details.put("order_id", "ORD-12345");
details.put("customer_id", "CUST-67890");
details.put("total_amount", 299.99);
details.put("currency", "USD");
details.put("processing_time_ms", 145);

auditLogger.logEvent("PAYMENT", "PROCESS", "payment_gateway", 
                   AuditResult.SUCCESS, "Payment processed successfully", details);
```

## JSON Output Format

The library produces line-delimited JSON logs with the following structure:

```json
{
  "timestamp": "2023-12-07T10:30:45.123Z",
  "event_type": "USER_ACTION",
  "user_id": "john.doe",
  "session_id": "session_12345",
  "application": "WebPortal",
  "component": "UserService",
  "action": "LOGIN",
  "resource": "/login",
  "result": "success",
  "message": "User logged in successfully",
  "correlation_id": "req_67890",
  "source_ip": "192.168.1.100",
  "user_agent": "Mozilla/5.0...",
  "details": {
    "custom_field": "custom_value"
  }
}
```

## Building and Packaging

### Building the Library

```bash
# Build the library
mvn clean compile

# Run tests
mvn test

# Create JAR with dependencies
mvn clean package

# Install to local Maven repository
mvn clean install
```

### Generated Artifacts

After building, you'll find these artifacts in the `target/` directory:
- `audit-logging-library-1.0.0.jar` - Main library JAR
- `audit-logging-library-1.0.0-sources.jar` - Source code JAR
- `audit-logging-library-1.0.0-javadoc.jar` - Javadoc JAR

### Using in Other Applications

#### Maven Projects

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.enterprise</groupId>
    <artifactId>audit-logging-library</artifactId>
    <version>1.0.0</version>
</dependency>
```

#### Gradle Projects

Add to your `build.gradle`:

```groovy
dependencies {
    implementation 'com.enterprise:audit-logging-library:1.0.0'
}
```

#### Manual JAR Usage

If using the JAR directly, ensure these dependencies are on your classpath:
- SLF4J API (1.7.36)
- Jackson Databind (2.13.5)
- Jackson JSR310 Module (2.13.5)

## Integration Examples

### Spring Boot Integration

```java
@Component
public class AuditService {
    
    private final AuditLogger auditLogger;
    
    public AuditService(@Value("${audit.log.directory:./logs}") String logDir) 
            throws AuditLoggingException {
        AuditConfiguration config = new AuditConfiguration();
        config.setLogDirectory(logDir);
        this.auditLogger = new FileSystemAuditLogger(config);
    }
    
    public void auditUserAction(String userId, String action, String resource, boolean success) {
        try {
            AuditContext.setUserId(userId);
            AuditContext.setApplication("MySpringApp");
            
            if (success) {
                auditLogger.logSuccess("USER_ACTION", action, resource, "Action completed");
            } else {
                auditLogger.logFailure("USER_ACTION", action, resource, "Action failed");
            }
        } catch (AuditLoggingException e) {
            // Handle logging failure appropriately
        } finally {
            AuditContext.clear();
        }
    }
    
    @PreDestroy
    public void cleanup() throws AuditLoggingException {
        auditLogger.close();
    }
}
```

### Web Filter Integration

```java
public class AuditFilter implements Filter {
    
    private AuditLogger auditLogger;
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        try {
            this.auditLogger = new FileSystemAuditLogger();
        } catch (AuditLoggingException e) {
            throw new ServletException("Failed to initialize audit logger", e);
        }
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Set audit context
            AuditContext.setSourceIp(httpRequest.getRemoteAddr());
            AuditContext.setUserAgent(httpRequest.getHeader("User-Agent"));
            AuditContext.setApplication("WebApp");
            
            chain.doFilter(request, response);
            
        } finally {
            // Log the request
            long duration = System.currentTimeMillis() - startTime;
            auditWebRequest(httpRequest, httpResponse, duration);
            AuditContext.clear();
        }
    }
    
    private void auditWebRequest(HttpServletRequest request, HttpServletResponse response, long duration) {
        try {
            AuditResult result = response.getStatus() < 400 ? AuditResult.SUCCESS : AuditResult.FAILURE;
            
            Map<String, Object> details = new HashMap<>();
            details.put("status_code", response.getStatus());
            details.put("duration_ms", duration);
            details.put("method", request.getMethod());
            
            auditLogger.logEvent("WEB_REQUEST", request.getMethod(), request.getRequestURI(),
                               result, "HTTP request processed", details);
                               
        } catch (AuditLoggingException e) {
            // Log error appropriately
        }
    }
    
    @Override
    public void destroy() {
        try {
            if (auditLogger != null) {
                auditLogger.close();
            }
        } catch (AuditLoggingException e) {
            // Log error appropriately
        }
    }
}
```

## Testing

Run the test suite:

```bash
mvn test
```

The library includes comprehensive tests covering:
- Basic logging functionality
- Configuration options
- Context propagation
- Error handling
- File system operations

## Log Shipping Integration

The library produces line-delimited JSON logs that are compatible with popular log shipping tools:

### Filebeat Configuration

```yaml
filebeat.inputs:
- type: log
  enabled: true
  paths:
    - /path/to/audit-logs/*.log
  json.keys_under_root: true
  json.add_error_key: true
  fields:
    log_type: audit
    application: your-app-name

output.elasticsearch:
  hosts: ["localhost:9200"]
  index: "audit-logs-%{+yyyy.MM.dd}"
```

### Logstash Configuration

```ruby
input {
  file {
    path => "/path/to/audit-logs/*.log"
    start_position => "beginning"
    codec => "json"
    tags => ["audit"]
  }
}

filter {
  if "audit" in [tags] {
    mutate {
      add_field => { "log_type" => "audit" }
    }
  }
}

output {
  elasticsearch {
    hosts => ["localhost:9200"]
    index => "audit-logs-%{+YYYY.MM.dd}"
  }
}
```

## Requirements

- Java 8 or higher
- SLF4J 1.7.x
- Jackson 2.13.x

## License

This library is licensed under the terms specified in the LICENSE file.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

## Support

For issues, questions, or contributions, please contact the development team or create an issue in the project repository.