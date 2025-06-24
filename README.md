# Enterprise Audit Logging Library

**Version 2.0.0**

A modern, Java 21+ audit logging library designed for cloud-native, large-scale enterprise environments. This library provides a uniform, async, and streamable way for applications to emit structured audit events, essential for compliance, security audits, and operational visibility. **This version is not backward compatible with previous versions.**

## Features
- **Java 21+ only**: Uses records, virtual threads, and modern Java features.
- **Streamable, async backend**: Non-blocking, virtual-thread-powered logging.
- **Structured JSON**: Line-delimited JSON events for easy ingestion.
- **Network streaming**: Sends events over TCP to Logstash or similar.
- **Cloud-native ready**: Designed for containers, Kubernetes, and distributed systems.

## Quick Start: Cloud-Native Usage

### 1. Add the Library

**Maven:**
```xml
<dependency>
  <groupId>com.enterprise</groupId>
  <artifactId>audit-logging-library</artifactId>
  <version>2.0.0</version>
</dependency>
```

### 2. Use `StreamableAuditLogger`

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