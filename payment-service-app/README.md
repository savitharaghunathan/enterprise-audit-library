# Payment Service Application

A Java 8 Spring Boot application that demonstrates the use of the Enterprise Audit Logging Library for comprehensive audit trail of payment operations.

## Overview

This payment service application showcases how to integrate the Enterprise Audit Logging Library into a real-world application to achieve compliance and operational visibility. The service processes payment requests and maintains detailed audit logs of all operations.

## Features

- **Payment Processing**: Process payment requests with validation
- **Audit Logging**: Comprehensive audit trail using the Enterprise Audit Logging Library
- **REST API**: RESTful endpoints for payment operations
- **Validation**: Input validation with detailed error messages
- **Health Monitoring**: Health check endpoint for monitoring
- **Comprehensive Testing**: Unit and integration tests with audit logging verification

## Technology Stack

- **Java 8**: Core language
- **Spring Boot 2.7.18**: Application framework
- **Enterprise Audit Logging Library 1.0.0**: Audit logging functionality
- **Jackson**: JSON serialization
- **SLF4J**: Logging framework
- **JUnit 4**: Testing framework
- **Mockito**: Mocking framework

## Project Structure

```
src/
├── main/
│   ├── java/com/enterprise/payment/
│   │   ├── PaymentServiceApplication.java    # Main Spring Boot application
│   │   ├── controller/
│   │   │   └── PaymentController.java        # REST API controller
│   │   ├── model/
│   │   │   ├── PaymentRequest.java           # Payment request model
│   │   │   ├── PaymentResponse.java          # Payment response model
│   │   │   └── PaymentStatus.java            # Payment status enum
│   │   └── service/
│   │       └── PaymentService.java           # Payment processing service
│   └── resources/
│       └── application.properties            # Application configuration
└── test/
    └── java/com/enterprise/payment/
        ├── PaymentServiceApplicationTests.java    # Application context test
        ├── controller/
        │   └── PaymentControllerTest.java         # Controller integration tests
        └── service/
            └── PaymentServiceTest.java            # Service unit tests
```

## API Endpoints

### Process Payment
```
POST /api/v1/payments/process
Content-Type: application/json

{
  "payment_id": "payment-123",
  "merchant_id": "merchant-456",
  "customer_id": "customer-789",
  "amount": 100.00,
  "currency": "USD",
  "payment_method": "CREDIT_CARD",
  "description": "Test payment"
}
```

### Get Payment Status
```
GET /api/v1/payments/{paymentId}/status
```

### Health Check
```
GET /api/v1/payments/health
```

## Audit Logging

The application uses the Enterprise Audit Logging Library to maintain comprehensive audit trails:

### Audit Events Logged

1. **Service Lifecycle Events**:
   - `SERVICE_STARTUP`: Service initialization
   - `SERVICE_SHUTDOWN`: Service shutdown

2. **Payment Processing Events**:
   - `PAYMENT_INITIATED`: Payment processing started
   - `PAYMENT_COMPLETED`: Payment successfully processed
   - `PAYMENT_DECLINED`: Payment declined
   - `PAYMENT_PROCESSED`: Payment processed with status
   - `PAYMENT_ERROR`: Payment processing error

3. **API Events**:
   - `API_REQUEST`: API request received
   - `API_RESPONSE`: API response sent
   - `VALIDATION_ERROR`: Request validation failed
   - `API_ERROR`: API processing error

4. **Status Events**:
   - `PAYMENT_STATUS_REQUESTED`: Status request received
   - `PAYMENT_STATUS_RETRIEVED`: Status retrieved successfully
   - `PAYMENT_STATUS_ERROR`: Status retrieval error

### Audit Context

The application sets up audit context for each request:
- **Correlation ID**: Unique identifier for request tracing
- **User ID**: Customer ID from payment request
- **Session ID**: Payment ID for session tracking
- **Application**: "payment-service"
- **Component**: "payment-processor" or "payment-api"
- **Source IP**: Client IP address
- **User Agent**: Client user agent

## Configuration

### Application Properties

```properties
# Server Configuration
server.port=8080

# Audit Logging Configuration
audit.log.directory=./payment-audit-logs
audit.log.file.prefix=payment
audit.log.file.extension=.log
audit.log.auto.create.directory=true
audit.log.append.mode=true
audit.log.max.file.size.mb=100
audit.log.max.files=10

# Logging Configuration
logging.level.com.enterprise.payment=INFO
logging.level.com.enterprise.audit=INFO
```

## Building and Running

### Prerequisites

- Java 8 or higher
- Maven 3.6 or higher
- Enterprise Audit Logging Library installed locally

### Build

```bash
mvn clean compile
```

### Run Tests

```bash
mvn test
```

### Run Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### Build JAR

```bash
mvn clean package
```

Run the JAR:
```bash
java -jar target/payment-service-1.0.0.jar
```

## Testing

### Unit Tests

The application includes comprehensive unit tests that verify:
- Payment processing logic
- Audit logging integration
- Error handling
- Processing fee calculations

### Integration Tests

Integration tests verify:
- REST API endpoints
- Request validation
- Response formatting
- Audit logging for API calls

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=PaymentServiceTest

# Run with detailed output
mvn test -Dtest=PaymentControllerTest -Dsurefire.useFile=false
```

## Example Usage

### Process a Payment

```bash
curl -X POST http://localhost:8080/api/v1/payments/process \
  -H "Content-Type: application/json" \
  -d '{
    "payment_id": "payment-123",
    "merchant_id": "merchant-456",
    "customer_id": "customer-789",
    "amount": 100.00,
    "currency": "USD",
    "payment_method": "CREDIT_CARD",
    "description": "Test payment"
  }'
```

### Get Payment Status

```bash
curl http://localhost:8080/api/v1/payments/payment-123/status
```

### Health Check

```bash
curl http://localhost:8080/api/v1/payments/health
```

## Audit Log Files

Audit logs are written to the configured directory (`./payment-audit-logs` by default) with the following structure:

```
payment-audit-logs/
├── payment-2024-01-15.log
├── payment-2024-01-16.log
└── ...
```

Each log entry is a JSON object containing:
- Timestamp
- Event type and details
- User and session information
- Request/response data
- Audit context information

## Payment Processing Logic

The service includes simulated payment processing logic:

- **Successful Payments**: Amounts between $0.01 and $10,000
- **Declined Payments**: Amounts over $10,000 or invalid payment methods
- **Failed Payments**: Amounts under $0.01
- **Processing Fees**: 2.9% of amount with $0.30 minimum

## Error Handling

The application includes comprehensive error handling:

- **Validation Errors**: Return 400 Bad Request with detailed messages
- **Processing Errors**: Return 500 Internal Server Error
- **Audit Logging**: All errors are logged with appropriate audit events

## Monitoring

### Health Check

The health check endpoint provides service status:
```json
{
  "status": "UP",
  "service": "payment-service",
  "version": "1.0.0"
}
```

### Logs

Monitor application logs for:
- Payment processing events
- Audit logging activities
- Error conditions
- Performance metrics

## Security Considerations

- Input validation on all requests
- Audit logging of all operations
- Client IP tracking
- User agent logging
- Correlation ID for request tracing

## Compliance Features

The audit logging provides compliance support for:
- **PCI DSS**: Payment card industry compliance
- **SOX**: Sarbanes-Oxley compliance
- **GDPR**: Data protection compliance
- **Internal Audits**: Operational compliance

## Future Enhancements

Potential improvements:
- Database integration for payment storage
- Real payment gateway integration
- Authentication and authorization
- Rate limiting
- Metrics and monitoring
- Docker containerization
- Kubernetes deployment 