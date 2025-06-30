# Audit Library Migration 
## v1 to v2 migration and custom rules

This document provides a backround on the audit logging library migration from v1 to v2, including context, migration points, and custom rules for automated migration analysis.

## Background

### Audit Library

Audit libraryy v1: https://github.com/savitharaghunathan/enterprise-audit-library/tree/dev-java8
Audit librray v2: https://github.com/savitharaghunathan/enterprise-audit-library/tree/dev-java21 

The audit library has evolved from v1 (Java 8) to v2 (Java 21+) to address modern enterprise requirements:


| **v1 Characteristics**                                | **v2 Characteristics**                           |
| ----------------------------------------------- | ------------------------------------------- |
| Java 8 compatibility                        | Java 21                   |
| Synchronous logging with filesystem backend         | Asynchronous logging with TCP-streaming backend |
| Builder pattern for `AuditEvent`                | Record-based `AuditEvent`                   |
| Convenience helpers `logSuccess` / `logFailure` | Full `AuditEvent` construction required     |
| Hard-coded configuration                        | Environment-based configuration             |
| Local file storage                              | Centralized logging via Logstash            |



### Migration drivers

**Technical:**
- Java 8 end-of-life and security concerns
- Performance bottlenecks from synchronous logging
- Scalability limitations of filesystem backend
- Need for centralized audit log management

**Business:**
- Compliance requirements
- Real-time audit monitoring capabilities
- Cloud native deployment patterns
- Operational efficiency improvements


## Migration area

### Core areas

The migration from v1 to v2 involves five primary areas of change:

1. **Dependency Management**: Upgrade audit library version
2. **Event Creation**: Replace builder pattern with record instantiation
3. **Logger Implementation**: Switch from filesystem to streaming logger
4. **Logging Methods**: Convert synchronous to asynchronous logging
5. **Convenience Methods**: Replace simplified methods with full audit events

### Impact Assessment

| Migration Area | Files Affected | Complexity | Risk Level |
|---------------|----------------|------------|------------|
| **Dependencies** | pom.xml | Low | Low |
| **Event Creation** | Service classes | Medium | Medium |
| **Logger Implementation** | Service classes | High | High |
| **Logging Methods** | Service classes | Medium | Medium |
| **Convenience Methods** | Service classes | High | Medium |



## Custom Rules

The following custom rules are created for automated migration analysis using Kai.

### Rule 1: Dependency Version Upgrade

```yaml
ruleID: audit-logging-0001
description: Detects Maven dependency on v1.x of audit-logging-library and suggests upgrading to 2.x.x (Java 21+)
category: mandatory
effort: 1
when:
  java.dependency:
    name: com.enterprise.audit-logging-library
    lowerbound: "1.0.0" 
    upperbound: "2.0.0" 
message: The `audit-logging-library` version {{dependency.version}} is outdated. Please upgrade to 2.0.0.
labels:
  - konveyor.io/source=openjdk8
  - konveyor.io/target=openjdk21
```

**Migration Point:**
- **File**: `pom.xml`
- **Change**: Update dependency version from 1.0.0 to 2.0.0
- **Impact**: Enables Java 21 features and new API capabilities

### Rule 2: Audit Event Builder Pattern

```yaml
ruleID: audit-logging-0002
description: Replace deprecated `AuditEvent.builder()` with direct Java 21 record instantiation
category: mandatory
effort: 3
when:
  java.referenced:
    pattern: com.enterprise.audit.logging.model.AuditEvent.builder
    location: IMPORT
message: The `AuditEvent.builder()` pattern is deprecated. Instantiate the `AuditEvent` record directly (e.g. `new AuditEvent(...)`).
labels:
  - konveyor.io/source=openjdk8
  - konveyor.io/target=openjdk21
```

**Migration Point:**
- **Pattern**: Replace builder pattern with direct record instantiation
- **Before**: `AuditEvent.builder().eventType("TYPE").build()`
- **After**: `new AuditEvent(timestamp, "TYPE", userId, ...)`
- **Impact**: Leverages Java 21 records for type safety and performance

### Rule 3: Logger Implementation

```yaml
ruleID: audit-logging-0003
description: Replace `FileSystemAuditLogger` instantiation with `StreamableAuditLogger` over TCP
category: mandatory
effort: 3
when:
  java.referenced:
    pattern: com.enterprise.audit.logging.service.FileSystemAuditLogger
    location: IMPORT 
message: Direct instantiation of `FileSystemAuditLogger` is deprecated. Use `StreamableAuditLogger` configured for TCP streaming.
labels:
  - konveyor.io/source=openjdk8
  - konveyor.io/target=openjdk21
```

**Migration Point:**
- **Pattern**: Replace FileSystemAuditLogger with StreamableAuditLogger
- **Before**: `new FileSystemAuditLogger(config)`
- **After**: `new StreamableAuditLogger(config)`
- **Impact**: Enables TCP streaming to centralized logging infrastructure

### Rule 4: Synchronous to Asynchronous Logging

```yaml
ruleID: audit-logging-0004
description: Use non-blocking `logEventAsync(event)` instead of synchronous `logEvent(event)`
category: mandatory
effort: 3
when:
  java.referenced:
    pattern: com.enterprise.audit.logging.service.FileSystemAuditLogger.logEvent
    location: METHOD_CALL
message: The synchronous `logEvent(event)` method should be replaced. Use the non-blocking `logEventAsync(event)` for better performance.
labels:
  - konveyor.io/source=openjdk8
  - konveyor.io/target=openjdk21
```

**Migration Point:**
- **Pattern**: Replace synchronous logging with asynchronous
- **Before**: `auditLogger.logEvent(auditEvent)`
- **After**: `auditLogger.logEventAsync(auditEvent)`
- **Impact**: Improves application performance by eliminating blocking operations

### Rule 5: Convenience Methods

```yaml
ruleID: audit-logging-0005
description: Replace legacy `logSuccess` or `logFailure` methods with a full `AuditEvent` record
category: mandatory
effort: 3
when:
  or:
    - java.referenced:
        pattern: com.enterprise.audit.logging.service.FileSystemAuditLogger.logSuccess
        location: METHOD_CALL
    - java.referenced:
        pattern: com.enterprise.audit.logging.service.FileSystemAuditLogger.logFailure
        location: METHOD_CALL
message: Legacy convenience methods (logSuccess, logFailure) are removed. Construct a full `AuditEvent` record and use logEventAsync instead.
labels:
  - konveyor.io/source=openjdk8
  - konveyor.io/target=openjdk21
```

**Migration Point:**
- **Pattern**: Replace convenience methods with full audit event construction
- **Before**: `auditLogger.logSuccess("TYPE", "ACTION", "RESOURCE", "MESSAGE")`
- **After**: Full `AuditEvent` construction with all required fields
- **Impact**: Provides complete audit context and enables advanced filtering

---

## Migration Analysis Results

### Sample Application Analysis

Analysis of the Inventory Management application reveals the following migration points.
Repo: https://github.com/savitharaghunathan/inventory_management/tree/java8
Analysis: using kantra

**Migration Incidents Found:**

| Issue                                                                                                    | Category  | Source      | Target      | Priority | Incidents | Effort |
| -------------------------------------------------------------------------------------------------------- | --------- | ----------- | ----------- | -------- | --------- | ------ |
| Detects Maven dependency on v1.x of audit-logging-library and suggests upgrading to 2.x.x (Java 21+) | mandatory | openjdk8    | openjdk21   | 1        | 1         | 1      |
| Replace `FileSystemAuditLogger` instantiation with `StreamableAuditLogger` over TCP                      | mandatory | openjdk8    | openjdk21   | 3        | 1         | 3      |
| Use non-blocking `logEventAsync(event)` instead of synchronous `logEvent(event)`                         | mandatory | openjdk8    | openjdk21   | 3        | 2         | 6      |
| Replace legacy `logSuccess` / `logFailure` with full `AuditEvent` record                                 | mandatory | openjdk8    | openjdk21   | 3        | 6         | 18     |
| `java.annotation` (Common Annotations) module removed in OpenJDK 11                                      | mandatory | openjdk ≤ 8 | openjdk 11+ | 1        | 2         | 2      |

Total of 5 issues and 12 incidents



## Migration Complexity 

### Technical 

**High Complexity :**
- Logger implementation changes (requires TCP configuration)
- Convenience method replacement (requires full audit event construction)
- Environment setup (requires Logstash infrastructure)

**Medium Complexity :**
- Audit event creation (builder to record pattern)
- Logging method changes (synchronous to asynchronous)

**Low Complexity :**
- Dependency version updates
- Import statement changes

### Risk 

**High Risk:**
- Logger implementation changes (potential connection issues)
- Configuration changes (environment-specific setup)

**Medium Risk:**
- Audit event creation (data structure changes)
- Convenience method replacement (increased code complexity)

**Low Risk:**
- Dependency updates (version compatibility)
- Import changes (syntax updates)


## Summary

The audit library migration from v1 to v2 involves systematic changes across dependency management, event creation, logger implementation, logging methods, and convenience method usage. The custom rules provide automated detection of migration points, enabling efficient planning and execution of the migration process.

The migration addresses modern enterprise requirements for performance, scalability, and centralized audit management while maintaining compliance and operational efficiency.
