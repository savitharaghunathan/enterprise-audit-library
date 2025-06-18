# Enterprise Audit Logging Library

**Version 2.0.0**

A modern, Java 21+ audit logging library designed for cloud-native, large-scale enterprise environments. This library provides a uniform, async, and streamable way for applications to emit structured audit events, essential for compliance, security audits, and operational visibility. **This version is not backward compatible with Java 8 or previous versions.**

## Version 2.0: Java 21+ Streamable Backend (Cloud-Native)

This version is designed for modern, cloud-native environments that feed into centralized logging platforms like the ELK Stack. It leverages Java 21 features and streams structured JSON audit events asynchronously over the network (e.g., via TCP to Logstash), minimizing performance impact on your application.

## Features

- **Java 21+ only**: Uses records, virtual threads, and modern Java features. Not compatible with Java 8 or earlier.
- **Streamable, async backend**: Non-blocking, virtual-thread-powered logging.
- **Structured JSON**: Line-delimited JSON events for easy ingestion.
- **Network streaming**: Sends events over TCP to Logstash or similar.
- **Cloud-native ready**: Designed for containers, Kubernetes, and distributed systems.

## Quick Start

### 1. Add Dependency

Add the library to your project:

**Maven:**
```xml
<dependency>
    <groupId>com.enterprise</groupId>
    <artifactId>audit-logging-library</artifactId>
    <version>2.0.0</version>
</dependency>
```

**Gradle:**
```groovy
implementation 'com.enterprise:audit-logging-library:2.0.0'
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

## Quick Start: Cloud-Native Usage

### 1. Use `StreamableAuditLogger`

```java
import com.enterprise.audit.logging.config.AuditConfiguration;
import com.enterprise.audit.logging.model.AuditEvent;
import com.enterprise.audit.logging.model.AuditResult;
import com.enterprise.audit.logging.service.StreamableAuditLogger;

AuditConfiguration config = new AuditConfiguration();
config.setStreamHost("logstash-host");
config.setStreamPort(5044); // Logstash TCP input port
StreamableAuditLogger logger = new StreamableAuditLogger(config);

AuditEvent event = new AuditEvent(
    null, // timestamp (or Instant.now())
    "USER_ACTION",
    "user1",
    null,
    "App",
    null,
    "LOGIN",
    "/login",
    AuditResult.SUCCESS,
    "User logged in successfully",
    null, // details
    null, // correlation_id
    null, // source_ip
    null  // user_agent
);
logger.logEventAsync(event); // Non-blocking, uses virtual threads
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

AuditEvent event = new AuditEvent(
    null, // timestamp (or Instant.now())
    "API_CALL",
    "api_user_123",
    "api_session_456",
    "RestAPI",
    "OrderController",
    "CREATE_ORDER",
    "/api/v1/orders",
    AuditResult.SUCCESS,
    "Order created successfully",
    null, // details
    "order_req_789",
    "10.0.0.15",
    "ApiClient/1.0"
);

auditLogger.logEventAsync(event); // Non-blocking, uses virtual threads
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

## Example: Logstash TCP Input Configuration

**Logstash pipeline config:**
```conf
input {
  tcp {
    port => 5044
    codec => json_lines
  }
}
output {
  elasticsearch {
    hosts => ["http://elasticsearch:9200"]
    index => "audit-logs-%{+YYYY.MM.dd}"
  }
  stdout { codec => rubydebug }
}
```

## Example: Filebeat Shipping (if using file-based fallback)

If you ever use a file-based logger, Filebeat config would look like:
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

## Deploying in Containers/Kubernetes

- **Stateless**: The logger does not persist state; it streams events directly.
- **Configurable**: Use environment variables or config maps to set host/port.
- **Resilient**: If Logstash is temporarily unavailable, the logger will throw, so use a circuit breaker or retry logic in your app if needed.
- **Resource-efficient**: Uses virtual threads for high concurrency with low overhead.

**Kubernetes Example:**
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: audit-logger-config
  namespace: your-namespace
  labels:
    app: your-app
  data:
    AUDIT_STREAM_HOST: "logstash-service"
    AUDIT_STREAM_PORT: "5044"
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: your-app
spec:
  template:
    spec:
      containers:
      - name: your-app
        image: your-app-image:latest
        env:
        - name: AUDIT_STREAM_HOST
          valueFrom:
            configMapKeyRef:
              name: audit-logger-config
              key: AUDIT_STREAM_HOST
        - name: AUDIT_STREAM_PORT
          valueFrom:
            configMapKeyRef:
              name: audit-logger-config
              key: AUDIT_STREAM_PORT
```

## Troubleshooting Java 21 and Test Runner Issues

- **Ensure all tools (Maven, IDE, test runner) use Java 21.**
- **Delete the `target/` directory manually if you see classpath or stale build errors.**
- **If using an IDE, do a full "Rebuild Project" or "Invalidate Caches and Restart".**
- **Check for duplicate or old `.class` files in your source tree.**
- **If you see `Map.of(...)` errors, your test runner is not using Java 9+; fix your environment.**

## Best Practices
- Use async logging (`logEventAsync`) for minimal performance impact.
- Monitor the health of your log shipping endpoint (Logstash, etc.).
- Use structured fields in your `AuditEvent` for easy querying in ELK.
- Use environment-based configuration for host/port in cloud deployments.

## Need Help?
If you have issues with cloud-native deployment, log shipping, or Java 21 setup, open an issue or contact the maintainers.