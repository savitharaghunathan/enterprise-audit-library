# Payment Service Application v2.0.0

A modern Spring Boot application that demonstrates payment processing with comprehensive audit logging using the Enterprise Audit Logging Library v2.

## Features

- **RESTful Payment API**: Process payments and retrieve payment status
- **Comprehensive Audit Logging**: TCP streaming backend with virtual threads
- **Java 21**: Latest LTS version with modern language features
- **Spring Boot 3.2.0**: Latest stable version with Jakarta EE
- **Input Validation**: Comprehensive request validation with detailed error messages
- **Error Handling**: Robust error handling with appropriate HTTP status codes
- **High Performance**: Virtual threads for improved concurrency

## Technology Stack

- **Java**: 21 (LTS)
- **Spring Boot**: 3.2.0
- **Audit Library**: Enterprise Audit Logging Library v2.0.0
- **Build Tool**: Maven 3.8+
- **Testing**: JUnit 5, Mockito 5, Spring Boot Test

## Prerequisites

- Java 21 or higher
- Maven 3.8 or higher
- Network access for TCP streaming (audit logs)

## Quick Start

### 1. Build the Application

```bash
mvn clean package
```

### 2. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### 3. Verify the Application

Check the health endpoint:

```bash
curl http://localhost:8080/api/v1/payments/health
```

Expected response:
```json
{
  "status": "UP",
  "service": "payment-service",
  "version": "2.0.0",
  "java": "21.0.1"
}
```

## API Endpoints

### Process Payment

**POST** `/api/v1/payments/process`

Process a payment request with comprehensive audit logging.

**Request Body:**
```json
{
  "payment_id": "payment-123",
  "customer_id": "customer-456",
  "merchant_id": "merchant-789",
  "amount": 100.00,
  "currency": "USD",
  "payment_method": "VISA",
  "description": "Online purchase"
}
```

**Response:**
```json
{
  "payment_id": "payment-123",
  "transaction_id": "TXN-ABC12345",
  "status": "COMPLETED",
  "amount": 100.00,
  "currency": "USD",
  "processing_fee": 2.90,
  "message": "Payment processed successfully",
  "response_timestamp": "2024-01-15T10:30:00"
}
```

### Get Payment Status

**GET** `/api/v1/payments/{paymentId}/status`

Retrieve the status of a payment.

**Response:**
```json
{
  "payment_id": "payment-123",
  "transaction_id": "TXN-ABC12345",
  "status": "COMPLETED",
  "amount": 100.00,
  "currency": "USD",
  "processing_fee": 2.90,
  "message": "Payment status retrieved",
  "response_timestamp": "2024-01-15T10:30:00"
}
```

### Health Check

**GET** `/api/v1/payments/health`

Check application health and version information.

## Configuration

### Audit Logging Configuration

The application uses TCP streaming for audit logs. Configure the following properties:

```properties
# TCP Streaming Configuration
audit.stream.host=localhost
audit.stream.port=5000
audit.stream.buffer.size=8192
audit.stream.timeout.ms=5000
audit.stream.auto.reconnect=true
audit.stream.max.retries=3
```

### Environment Variables

You can override configuration using environment variables:

```bash
export AUDIT_STREAM_HOST=logstash.example.com
export AUDIT_STREAM_PORT=5000
export AUDIT_STREAM_BUFFER_SIZE=16384
```

## Testing

### Run All Tests

```bash
mvn test
```

### Run Specific Test Classes

```bash
mvn test -Dtest=PaymentServiceTest
mvn test -Dtest=PaymentControllerTest
```

### Run Integration Tests

```bash
mvn test -Dtest=PaymentServiceApplicationTests
```

## Audit Logging

The application uses the Enterprise Audit Logging Library v2 with the following features:

- **TCP Streaming**: Asynchronous audit events sent over TCP
- **Virtual Threads**: High-performance concurrent logging
- **Structured Events**: JSON-formatted audit events
- **Comprehensive Coverage**: All payment operations are audited

### Audit Events

The application logs the following audit events:

- `PAYMENT_INITIATED`: When payment processing starts
- `PAYMENT_COMPLETED`: When payment is successfully processed
- `PAYMENT_DECLINED`: When payment is declined
- `PAYMENT_PROCESSED`: When payment processing completes with status
- `PAYMENT_ERROR`: When payment processing fails
- `PAYMENT_STATUS_REQUESTED`: When payment status is requested
- `PAYMENT_STATUS_RETRIEVED`: When payment status is retrieved
- `SERVICE_STARTUP`: When the service starts
- `SERVICE_SHUTDOWN`: When the service shuts down

## Migration from v1

This version represents a major upgrade from v1. Key changes include:

### Java Version
- **v1**: Java 8
- **v2**: Java 21

### Spring Boot Version
- **v1**: Spring Boot 2.7.18
- **v2**: Spring Boot 3.2.0

### Audit Library
- **v1**: FileSystemAuditLogger with filesystem backend
- **v2**: StreamableAuditLogger with TCP streaming backend

### API Changes
- **v1**: Synchronous logging methods
- **v2**: Asynchronous logging with CompletableFuture

### Event Creation
- **v1**: `AuditEvent.builder()...build()`
- **v2**: `new AuditEvent(...)` (Java record)

## Development

### Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/enterprise/payment/
│   │       ├── controller/
│   │       │   └── PaymentController.java
│   │       ├── model/
│   │       │   ├── PaymentRequest.java
│   │       │   ├── PaymentResponse.java
│   │       │   └── PaymentStatus.java
│   │       ├── service/
│   │       │   └── PaymentService.java
│   │       └── PaymentServiceApplication.java
│   └── resources/
│       └── application.properties
└── test/
    └── java/
        └── com/enterprise/payment/
            ├── controller/
            │   └── PaymentControllerTest.java
            ├── service/
            │   └── PaymentServiceTest.java
            └── PaymentServiceApplicationTests.java
```

### Adding New Features

1. Create model classes in the `model` package
2. Add business logic in the `service` package
3. Create REST endpoints in the `controller` package
4. Add comprehensive tests
5. Update audit logging for new operations

## Troubleshooting

### Common Issues

1. **Port Already in Use**: Change the port in `application.properties`
2. **TCP Connection Failed**: Verify audit stream host and port configuration
3. **Validation Errors**: Check request format and required fields

### Logs

Application logs are written to the console. Look for:
- Spring Boot startup messages
- Audit logging events
- Payment processing details
- Error messages

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For issues and questions:
1. Check the troubleshooting section
2. Review the audit logging configuration
3. Verify Java 21 is installed and configured
4. Check network connectivity for TCP streaming 