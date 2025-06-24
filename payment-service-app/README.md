# Payment Service Application

A Java 8 Spring Boot application that demonstrates the use of the Enterprise Audit Logging Library for comprehensive audit trail of payment operations.

## Overview

This payment service application showcases how to integrate the Enterprise Audit Logging Library into a real-world application to achieve compliance and operational visibility. The service processes payment requests through a simulated payment gateway and maintains detailed audit logs of all operations.

## Features

- **Realistic Payment Processing**: Process payment requests with credit card validation and gateway simulation
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
│   │   │   ├── PaymentStatus.java            # Payment status enum
│   │   │   ├── BillingAddress.java           # Billing address model
│   │   │   └── GatewayResponse.java          # Gateway response model
│   │   ├── gateway/
│   │   │   ├── PaymentGateway.java           # Payment gateway interface
│   │   │   ├── MockPaymentGateway.java       # Mock gateway implementation
│   │   │   └── GatewayException.java         # Gateway exception
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
  "description": "Test payment",
  "card_number": "4242424242424242",
  "expiry_month": 12,
  "expiry_year": 2026,
  "cvv": "123",
  "cardholder_name": "Jane Doe",
  "billing_address": {
    "line1": "123 Main St",
    "line2": "Apt 4B",
    "city": "New York",
    "state": "NY",
    "postal_code": "10001",
    "country": "US"
  }
}
```

### Get Payment Status (Mock)
```
GET /api/v1/payments/{paymentId}
```

### Health Check
```
GET /api/v1/payments/health
```

## Audit Logging

The application uses the Enterprise Audit Logging Library to maintain comprehensive audit trails:

### Audit Events Logged

1. **Payment Processing Events**:
   - `PAYMENT_INITIATED`: Payment processing started
   - `PAYMENT_PROCESSED`: Payment processed successfully
   - `PAYMENT_GATEWAY_ERROR`: Payment gateway error
   - `PAYMENT_ERROR`: Payment processing error

2. **API Events**:
   - `API_REQUEST`: API request received
   - `API_RESPONSE`: API response sent
   - `VALIDATION_ERROR`: Request validation failed
   - `API_ERROR`: API processing error

### Audit Context

The application sets up audit context for each request:
- **Payment ID**: Unique identifier for payment tracking
- **Amount and Currency**: Payment details
- **Payment Method**: Type of payment method used
- **Client IP**: Client IP address
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
- Payment processing logic with gateway integration
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
    "description": "Test payment",
    "card_number": "4242424242424242",
    "expiry_month": 12,
    "expiry_year": 2026,
    "cvv": "123",
    "cardholder_name": "Jane Doe",
    "billing_address": {
      "line1": "123 Main St",
      "line2": "Apt 4B",
      "city": "New York",
      "state": "NY",
      "postal_code": "10001",
      "country": "US"
    }
  }'
```

### Test Different Payment Scenarios

**Successful Payment (Default):**
```bash
curl -X POST http://localhost:8080/api/v1/payments/process \
  -H "Content-Type: application/json" \
  -d '{
    "payment_id": "payment-success",
    "merchant_id": "merchant-456",
    "customer_id": "customer-789",
    "amount": 100.00,
    "currency": "USD",
    "payment_method": "CREDIT_CARD",
    "description": "Successful payment test",
    "card_number": "4242424242424242",
    "expiry_month": 12,
    "expiry_year": 2026,
    "cvv": "123",
    "cardholder_name": "Jane Doe",
    "billing_address": {
      "line1": "123 Main St",
      "city": "New York",
      "state": "NY",
      "postal_code": "10001",
      "country": "US"
    }
  }'
```

**Declined Card:**
```bash
curl -X POST http://localhost:8080/api/v1/payments/process \
  -H "Content-Type: application/json" \
  -d '{
    "payment_id": "payment-declined",
    "merchant_id": "merchant-456",
    "customer_id": "customer-789",
    "amount": 100.00,
    "currency": "USD",
    "payment_method": "CREDIT_CARD",
    "description": "Declined card test",
    "card_number": "4000000000000002",
    "expiry_month": 12,
    "expiry_year": 2026,
    "cvv": "123",
    "cardholder_name": "Jane Doe",
    "billing_address": {
      "line1": "123 Main St",
      "city": "New York",
      "state": "NY",
      "postal_code": "10001",
      "country": "US"
    }
  }'
```

**Insufficient Funds:**
```bash
curl -X POST http://localhost:8080/api/v1/payments/process \
  -H "Content-Type: application/json" \
  -d '{
    "payment_id": "payment-insufficient",
    "merchant_id": "merchant-456",
    "customer_id": "customer-789",
    "amount": 100.00,
    "currency": "USD",
    "payment_method": "CREDIT_CARD",
    "description": "Insufficient funds test",
    "card_number": "4000000000009995",
    "expiry_month": 12,
    "expiry_year": 2026,
    "cvv": "123",
    "cardholder_name": "Jane Doe",
    "billing_address": {
      "line1": "123 Main St",
      "city": "New York",
      "state": "NY",
      "postal_code": "10001",
      "country": "US"
    }
  }'
```

**Large Amount (Exceeds Limit):**
```bash
curl -X POST http://localhost:8080/api/v1/payments/process \
  -H "Content-Type: application/json" \
  -d '{
    "payment_id": "payment-large",
    "merchant_id": "merchant-456",
    "customer_id": "customer-789",
    "amount": 15000.00,
    "currency": "USD",
    "payment_method": "CREDIT_CARD",
    "description": "Large amount test",
    "card_number": "4242424242424242",
    "expiry_month": 12,
    "expiry_year": 2026,
    "cvv": "123",
    "cardholder_name": "Jane Doe",
    "billing_address": {
      "line1": "123 Main St",
      "city": "New York",
      "state": "NY",
      "postal_code": "10001",
      "country": "US"
    }
  }'
```

### View Audit Logs

Audit logs are written to the configured directory (`./payment-audit-logs` by default). To view logs in real time:

```bash
# View all audit logs
tail -f ./payment-audit-logs/payment-*.log

# View specific log file
tail -f ./payment-audit-logs/payment-2024-01-15.log

# View last 50 lines
tail -50 ./payment-audit-logs/payment-*.log
```

No Logstash or external log aggregator is required. All audit logs are local files.

### Health Check

```bash
curl http://localhost:8080/api/v1/payments/health
```

### Requirements
- Java 8
- Enterprise Audit Logging Library v1.x
- No Logstash required

## Payment Processing Logic

The service includes realistic payment processing logic through a simulated payment gateway:

- **Successful Payments**: Most payments with valid card details (95%+ success rate)
- **Declined Payments**: Specific test card numbers, expired cards, invalid CVV
- **Failed Payments**: Network timeouts, gateway errors, insufficient funds
- **Processing Fees**: 2.9% of amount with $0.30 minimum
- **Realistic Delays**: 1-3 second processing time simulation

## Error Handling

The application includes comprehensive error handling:

- **Validation Errors**: Return 400 Bad Request with detailed messages
- **Gateway Errors**: Return 500 Internal Server Error
- **Audit Logging**: All errors are logged with appropriate audit events

## Security Considerations

- Input validation on all requests
- Audit logging of all operations
- Client IP tracking
- User agent logging
- Payment gateway abstraction for security

## Compliance Features

The audit logging provides compliance support for:
- **PCI DSS**: Payment card industry compliance
- **SOX**: Sarbanes-Oxley compliance
- **GDPR**: Data protection compliance
- **Internal Audits**: Operational compliance 