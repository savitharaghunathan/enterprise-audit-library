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
- **Logstash running on localhost:5000** (required for audit logging)
- Network access for TCP streaming (audit logs)

## Quick Start

### 1. Start Logstash (Required)

The payment service requires Logstash to be running for audit logging. Start Logstash first:

```bash
# Using Podman (recommended)
podman run -d --name logstash -p 5000:5000 \
  -v $(pwd)/logstash.conf:/usr/share/logstash/pipeline/logstash.conf \
  -v $(pwd)/logstash.yml:/usr/share/logstash/config/logstash.yml \
  docker.elastic.co/logstash/logstash:8.11.0

# Using Docker (alternative)
docker run -d --name logstash -p 5000:5000 \
  -v $(pwd)/logstash.conf:/usr/share/logstash/pipeline/logstash.conf \
  -v $(pwd)/logstash.yml:/usr/share/logstash/config/logstash.yml \
  docker.elastic.co/logstash/logstash:8.11.0

# Or using a local Logstash installation
# Ensure Logstash is configured to listen on port 5000 for TCP input
```

**Note:** The application will fail to start if Logstash is not available. This is intentional for production environments where audit logging is critical.

### 2. Build the Application

```bash
mvn clean package
```

### 3. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### 4. Verify the Application

Check the health endpoint:

```bash
curl http://localhost:8080/api/v1/payments/health
```

Expected response:
```json
{
  "java": "21.0.6",
  "service": "payment-service",
  "version": "2.0.0",
  "status": "UP"
}
```

Check detailed health including audit logging status:

```bash
curl http://localhost:8080/api/v1/payments/health/detailed
```

Expected response:
```json
{
  "java": "21.0.6",
  "service": "payment-service",
  "version": "2.0.0",
  "status": "UP",
  "audit_logging": "UP",
  "audit_stream_host": "localhost",
  "audit_stream_port": 5000
}
```

### 5. View Audit Logs

After processing payments, you can view the audit events in Logstash:

```bash
# View all audit events
podman exec logstash cat /tmp/audit-events.log

# View recent events
podman exec logstash tail -f /tmp/audit-events.log

# Check Logstash status
podman logs logstash | grep -E "(Pipeline|tcp|Started)"

# Stop Logstash when done
podman stop logstash && podman rm logstash
```

## API Testing with Curl

### ✅ Working Test Commands

All commands below have been tested and validated with the payment service.

#### 1. Process a Payment (Complete Example)

```bash
curl -X POST http://localhost:8080/api/v1/payments/process \
  -H "Content-Type: application/json" \
  -d '{
    "payment_id": "test-payment-123",
    "customer_id": "customer-123",
    "merchant_id": "merchant-456",
    "amount": 100.00,
    "currency": "USD",
    "payment_method": "VISA",
    "description": "Test payment"
  }'
```

**Expected Response:**
```json
{
  "payment_id": "test-payment-123",
  "transaction_id": "TXN-1D0E42E1",
  "status": "COMPLETED",
  "amount": 100.00,
  "currency": "USD",
  "processing_fee": 2.90000,
  "message": "Payment processed successfully",
  "response_timestamp": "2025-06-24T17:18:29"
}
```

#### 2. Process a Payment (Minimal Required Fields)

```bash
curl -X POST http://localhost:8080/api/v1/payments/process \
  -H "Content-Type: application/json" \
  -d '{
    "payment_id": "minimal-test",
    "customer_id": "cust-1",
    "merchant_id": "merch-1",
    "amount": 50.00,
    "currency": "EUR",
    "payment_method": "MASTERCARD"
  }'
```

**Expected Response:**
```json
{
  "payment_id": "minimal-test",
  "transaction_id": "TXN-8C13AFFB",
  "status": "COMPLETED",
  "amount": 50.00,
  "currency": "EUR",
  "processing_fee": 1.45000,
  "message": "Payment processed successfully",
  "response_timestamp": "2025-06-24T17:18:36"
}
```

#### 3. Get Payment Status

```bash
curl http://localhost:8080/api/v1/payments/test-payment-123/status
```

**Expected Response:**
```json
{
  "payment_id": "test-payment-123",
  "transaction_id": "TXN-TEST-PAY",
  "status": "COMPLETED",
  "amount": 100.00,
  "currency": "USD",
  "message": "Payment status retrieved",
  "response_timestamp": "2025-06-24T17:18:43"
}
```

#### 4. Health Check

```bash
curl http://localhost:8080/api/v1/payments/health
```

**Expected Response:**
```json
{
  "java": "21.0.6",
  "service": "payment-service",
  "version": "2.0.0",
  "status": "UP"
}
```

### 🔑 Important Notes

- **Use snake_case field names**: `payment_id`, `customer_id`, `merchant_id`, `payment_method`
- **Required fields**: `payment_id`, `customer_id`, `merchant_id`, `amount`, `currency`, `payment_method`
- **Currency format**: Must be 3 uppercase letters (USD, EUR, GBP, etc.)
- **Amount minimum**: Must be at least 0.01
- **Processing fee**: Automatically calculated as 2.9% of the payment amount

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
  "transaction_id": "TXN-1D0E42E1",
  "status": "COMPLETED",
  "amount": 100.00,
  "currency": "USD",
  "processing_fee": 2.90000,
  "message": "Payment processed successfully",
  "response_timestamp": "2025-06-24T17:18:29"
}
```

### Get Payment Status

**GET** `/api/v1/payments/{paymentId}/status`

Retrieve the status of a payment.

**Response:**
```json
{
  "payment_id": "payment-123",
  "transaction_id": "TXN-TEST-PAY",
  "status": "COMPLETED",
  "amount": 100.00,
  "currency": "USD",
  "message": "Payment status retrieved",
  "response_timestamp": "2025-06-24T17:18:43"
}
```

### Health Check

**GET** `/api/v1/payments/health`

Check application health and version information.

**Response:**
```json
{
  "java": "21.0.6",
  "service": "payment-service",
  "version": "2.0.0",
  "status": "UP"
}
```

## Configuration

### Logstash Configuration Files

The payment service includes pre-configured Logstash files:

- **`logstash.conf`** - Pipeline configuration for TCP input and file output
- **`logstash.yml`** - Logstash settings to disable monitoring and optimize performance

These files are automatically mounted when using the Podman/Docker commands in the Quick Start section.

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
4. **Application Fails to Start**: Ensure Logstash is running on localhost:5000

### Audit Logging Issues

#### Application Won't Start - "Audit logging backend unavailable"

**Error:**
```
java.lang.RuntimeException: Audit logging backend unavailable. Cannot connect to localhost:5000. Please ensure Logstash is running.
```

**Solution:**
1. Start Logstash before running the payment service
2. Verify Logstash is listening on port 5000
3. Check firewall settings
4. Ensure no other service is using port 5000

#### Check Logstash Status

```bash
# Check if Logstash is running (Podman)
podman ps | grep logstash

# Check if Logstash is running (Docker)
docker ps | grep logstash

# Check if port 5000 is listening
lsof -i :5000

# Test Logstash connectivity
telnet localhost 5000

# View Logstash logs (Podman)
podman logs logstash

# View Logstash logs (Docker)
docker logs logstash
```

#### Logstash Configuration

Ensure your Logstash configuration includes TCP input on port 5000:

```conf
input {
  tcp {
    port => 5000
    codec => json
  }
}

output {
  stdout {
    codec => json
  }
  # Add other outputs as needed (Elasticsearch, etc.)
}
```

### Logs

Application logs are written to the console. Look for:
- Spring Boot startup messages
- Audit logging events
- Payment processing details
- Error messages
- Logstash connection status

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For issues and questions:
1. Check the troubleshooting section
2. Review the audit logging configuration
3. Verify Java 21 is installed and configured
4. Check network connectivity for TCP streaming 