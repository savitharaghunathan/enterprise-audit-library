# Migration Guide: Java 8 (v1) → Java 21 (v2) Audit Logging Library

This guide will help you migrate your application from the legacy Java 8 (v1) synchronous, file-based audit logging library to the new Java 21 (v2) async, streamable, cloud-native version.

---

## 1. Update Your Dependencies

**Java 8 (v1):**
```xml
<dependency>
  <groupId>com.enterprise</groupId>
  <artifactId>audit-logging-library</artifactId>
  <version>1.x.x</version>
</dependency>
```

**Java 21 (v2):**
```xml
<dependency>
  <groupId>com.enterprise</groupId>
  <artifactId>audit-logging-library</artifactId>
  <version>2.x.x</version>
</dependency>
```
- Ensure your Maven/Gradle build and runtime use Java 21+.

---

## 2. Change Event Creation

**Java 8 (v1):**
```java
AuditEvent event = AuditEvent.builder()
    .eventType("USER_ACTION")
    .userId("user123")
    .sessionId("session456")
    .application("MyApp")
    .component("LoginService")
    .action("LOGIN")
    .resource("/login")
    .result(AuditResult.SUCCESS)
    .message("User logged in successfully")
    .correlationId("corr-789")
    .sourceIp("10.0.0.1")
    .userAgent("Mozilla/5.0")
    .build();
```

**Java 21 (v2):**
```java
AuditEvent event = new AuditEvent(
    null, // timestamp (or Instant.now())
    "USER_ACTION",
    "user123",
    "session456",
    "MyApp",
    "LoginService",
    "LOGIN",
    "/login",
    AuditResult.SUCCESS,
    "User logged in successfully",
    null, // details
    "corr-789",
    "10.0.0.1",
    "Mozilla/5.0"
);
```
- **No builder pattern.** Use the record constructor directly.

---

## 3. Change Logger Instantiation

**Java 8 (v1):**
```java
AuditConfiguration config = new AuditConfiguration();
config.setLogDirectory("/var/log/myapp/audit");
FileSystemAuditLogger logger = new FileSystemAuditLogger(config);
```

**Java 21 (v2):**
```java
AuditConfiguration config = new AuditConfiguration();
config.setStreamHost("logstash-host");
config.setStreamPort(5044); // Logstash TCP input port
StreamableAuditLogger logger = new StreamableAuditLogger(config);
```
- **No file-based logger.** Use the streamable logger and configure for network.

---

## 4. Change Logging Method

**Java 8 (v1):**
```java
logger.logEvent(event); // Synchronous, blocking
```

**Java 21 (v2):**
```java
logger.logEventAsync(event); // Asynchronous, non-blocking, virtual thread
```
- **Async API:** Returns a `CompletableFuture<Void>`. You can `.get()` if you want to block for completion.

---

## 5. Remove Legacy Convenience Methods

**Java 8 (v1):**
```java
logger.logSuccess(...);
logger.logFailure(...);
logger.logDenied(...);
```

**Java 21 (v2):**
- **No convenience methods.**  
  Always construct the `AuditEvent` record and call `logEventAsync`.

---

## 6. Update Context Propagation (if used)

**Java 8 (v1):**
```java
AuditContext.setUserId("user123");
```

**Java 21 (v2):**
- Still supported, but ensure you use it before constructing the event if you want context auto-population.

---

## 7. Update Tests and Integration
- Use a mock TCP server for integration tests (see `StreamableAuditLoggerTest`).
- Remove any file-based test logic.

---

## 8. Update Configuration and Deployment
- Use environment variables or config maps for host/port in cloud-native deployments.
- No need to mount persistent volumes for log files.

---

## Summary Table

| Step                | Java 8 (v1) Example                | Java 21 (v2) Example                |
|---------------------|-------------------------------------|-------------------------------------|
| Event Creation      | `AuditEvent.builder()...build();`   | `new AuditEvent(...);`              |
| Logger Instantiation| `FileSystemAuditLogger`             | `StreamableAuditLogger`             |
| Logging Call        | `logEvent(event)`                   | `logEventAsync(event)`              |
| Backend             | Filesystem                          | TCP/Logstash                        |
| Java Version        | 8+                                  | 21+ only                            |
| API Style           | Builder, sync                       | Record, async                       |

---

## Full Java 21 Example

```java
import com.enterprise.audit.logging.model.AuditEvent;
import com.enterprise.audit.logging.model.AuditResult;
import com.enterprise.audit.logging.config.AuditConfiguration;
import com.enterprise.audit.logging.service.StreamableAuditLogger;

AuditConfiguration config = new AuditConfiguration();
config.setStreamHost("logstash-host");
config.setStreamPort(5044);
StreamableAuditLogger logger = new StreamableAuditLogger(config);

AuditEvent event = new AuditEvent(
    null, "USER_ACTION", "user123", "session456", "MyApp", "LoginService",
    "LOGIN", "/login", AuditResult.SUCCESS, "User logged in successfully",
    null, "corr-789", "10.0.0.1", "Mozilla/5.0"
);

logger.logEventAsync(event); // Async, non-blocking
logger.close();
```

