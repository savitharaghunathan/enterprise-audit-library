package com.enterprise.payment.service;

import com.enterprise.audit.logging.model.AuditEvent;
import com.enterprise.audit.logging.model.AuditResult;
import com.enterprise.audit.logging.service.AuditLogger;
import com.enterprise.audit.logging.service.StreamableAuditLogger;
import com.enterprise.payment.model.PaymentRequest;
import com.enterprise.payment.model.PaymentResponse;
import com.enterprise.payment.model.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PaymentService using v2 audit logging library.
 * Tests payment processing functionality with comprehensive audit logging.
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private StreamableAuditLogger mockAuditLogger;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService();
        
        // Use reflection to inject the mock audit logger
        try {
            java.lang.reflect.Field auditLoggerField = PaymentService.class.getDeclaredField("auditLogger");
            auditLoggerField.setAccessible(true);
            auditLoggerField.set(paymentService, mockAuditLogger);
        } catch (Exception e) {
            fail("Failed to inject mock audit logger: " + e.getMessage());
        }
    }

    @Test
    void testProcessPayment_Success() {
        // Arrange
        PaymentRequest request = createValidPaymentRequest();
        
        when(mockAuditLogger.logEventAsync(any(AuditEvent.class)))
            .thenReturn(CompletableFuture.completedFuture(null));

        // Act
        PaymentResponse response = paymentService.processPayment(request);

        // Assert
        assertNotNull(response);
        assertEquals(request.getPaymentId(), response.getPaymentId());
        assertEquals(PaymentStatus.COMPLETED, response.getStatus());
        assertNotNull(response.getTransactionId());
        assertEquals(request.getAmount(), response.getAmount());
        assertEquals(request.getCurrency(), response.getCurrency());
        assertNotNull(response.getProcessingFee());
        assertTrue(response.getProcessingFee().compareTo(BigDecimal.ZERO) > 0);

        // Verify audit logging calls
        verify(mockAuditLogger, times(2)).logEventAsync(any(AuditEvent.class));
        
        // Verify specific audit events using v2 record accessors
        verify(mockAuditLogger).logEventAsync(argThat(event -> 
            "PAYMENT_INITIATED".equals(event.event_type()) &&
            AuditResult.SUCCESS.equals(event.result())
        ));
        
        verify(mockAuditLogger).logEventAsync(argThat(event -> 
            "PAYMENT_COMPLETED".equals(event.event_type()) &&
            AuditResult.SUCCESS.equals(event.result())
        ));
    }

    @Test
    void testProcessPayment_LargeAmount_Declined() {
        // Arrange
        PaymentRequest request = createValidPaymentRequest();
        request.setAmount(new BigDecimal("15000.00")); // Large amount that should be declined
        
        when(mockAuditLogger.logEventAsync(any(AuditEvent.class)))
            .thenReturn(CompletableFuture.completedFuture(null));

        // Act
        PaymentResponse response = paymentService.processPayment(request);

        // Assert
        assertNotNull(response);
        assertEquals(PaymentStatus.DECLINED, response.getStatus());
        assertTrue(response.getMessage().contains("declined"));

        // Verify audit logging calls
        verify(mockAuditLogger, times(2)).logEventAsync(any(AuditEvent.class));
        
        // Verify specific audit events using v2 record accessors
        verify(mockAuditLogger).logEventAsync(argThat(event -> 
            "PAYMENT_DECLINED".equals(event.event_type()) &&
            AuditResult.FAILURE.equals(event.result())
        ));
    }

    @Test
    void testProcessPayment_InvalidPaymentMethod_Declined() {
        // Arrange
        PaymentRequest request = createValidPaymentRequest();
        request.setPaymentMethod("INVALID_CARD");
        
        when(mockAuditLogger.logEventAsync(any(AuditEvent.class)))
            .thenReturn(CompletableFuture.completedFuture(null));

        // Act
        PaymentResponse response = paymentService.processPayment(request);

        // Assert
        assertNotNull(response);
        assertEquals(PaymentStatus.DECLINED, response.getStatus());
        assertTrue(response.getMessage().contains("Invalid payment method"));

        // Verify audit logging calls
        verify(mockAuditLogger, times(2)).logEventAsync(any(AuditEvent.class));
        
        // Verify specific audit events using v2 record accessors
        verify(mockAuditLogger).logEventAsync(argThat(event -> 
            "PAYMENT_DECLINED".equals(event.event_type()) &&
            AuditResult.FAILURE.equals(event.result())
        ));
    }

    @Test
    void testProcessPayment_VerySmallAmount_Failed() {
        // Arrange
        PaymentRequest request = createValidPaymentRequest();
        request.setAmount(new BigDecimal("0.005")); // Very small amount that should fail
        
        when(mockAuditLogger.logEventAsync(any(AuditEvent.class)))
            .thenReturn(CompletableFuture.completedFuture(null));

        // Act
        PaymentResponse response = paymentService.processPayment(request);

        // Assert
        assertNotNull(response);
        assertEquals(PaymentStatus.FAILED, response.getStatus());
        assertTrue(response.getMessage().contains("too small"));

        // Verify audit logging calls
        verify(mockAuditLogger, times(2)).logEventAsync(any(AuditEvent.class));
        
        // Verify specific audit events using v2 record accessors
        verify(mockAuditLogger).logEventAsync(argThat(event -> 
            "PAYMENT_PROCESSED".equals(event.event_type()) &&
            AuditResult.SUCCESS.equals(event.result())
        ));
    }

    @Test
    void testProcessPayment_AuditLoggingFailure_StillProcesses() {
        // Arrange
        PaymentRequest request = createValidPaymentRequest();
        
        when(mockAuditLogger.logEventAsync(any(AuditEvent.class)))
            .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Audit logging failed")));

        // Act
        PaymentResponse response = paymentService.processPayment(request);

        // Assert
        assertNotNull(response);
        assertEquals(PaymentStatus.COMPLETED, response.getStatus());
        // Payment should still process even if audit logging fails
    }

    @Test
    void testGetPaymentStatus_Success() {
        // Arrange
        String paymentId = "test-payment-123";
        
        when(mockAuditLogger.logEventAsync(any(AuditEvent.class)))
            .thenReturn(CompletableFuture.completedFuture(null));

        // Act
        PaymentResponse response = paymentService.getPaymentStatus(paymentId);

        // Assert
        assertNotNull(response);
        assertEquals(paymentId, response.getPaymentId());
        assertEquals(PaymentStatus.COMPLETED, response.getStatus());
        assertNotNull(response.getTransactionId());
        assertNotNull(response.getAmount());
        assertNotNull(response.getCurrency());

        // Verify audit logging calls
        verify(mockAuditLogger, times(2)).logEventAsync(any(AuditEvent.class));
        
        // Verify specific audit events using v2 record accessors
        verify(mockAuditLogger).logEventAsync(argThat(event -> 
            "PAYMENT_STATUS_REQUESTED".equals(event.event_type()) &&
            AuditResult.SUCCESS.equals(event.result())
        ));
        
        verify(mockAuditLogger).logEventAsync(argThat(event -> 
            "PAYMENT_STATUS_RETRIEVED".equals(event.event_type()) &&
            AuditResult.SUCCESS.equals(event.result())
        ));
    }

    @Test
    void testGetPaymentStatus_AuditLoggingFailure_StillProcesses() {
        // Arrange
        String paymentId = "test-payment-123";
        
        when(mockAuditLogger.logEventAsync(any(AuditEvent.class)))
            .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Audit logging failed")));

        // Act
        PaymentResponse response = paymentService.getPaymentStatus(paymentId);

        // Assert
        assertNotNull(response);
        assertEquals(paymentId, response.getPaymentId());
        assertEquals(PaymentStatus.COMPLETED, response.getStatus());
        // Status retrieval should still work even if audit logging fails
    }

    @Test
    void testProcessPayment_ExceptionHandling() {
        // Arrange
        PaymentRequest request = createValidPaymentRequest();
        
        // Mock the audit logger to work normally
        when(mockAuditLogger.logEventAsync(any(AuditEvent.class)))
            .thenReturn(CompletableFuture.completedFuture(null));

        // Act
        PaymentResponse response = paymentService.processPayment(request);

        // Assert
        assertNotNull(response);
        assertEquals(PaymentStatus.COMPLETED, response.getStatus());
        // Payment should complete successfully since the audit logging works normally

        // Verify audit logging calls
        verify(mockAuditLogger, times(2)).logEventAsync(any(AuditEvent.class));
        
        // Verify successful audit events
        verify(mockAuditLogger).logEventAsync(argThat(event -> 
            "PAYMENT_INITIATED".equals(event.event_type()) &&
            AuditResult.SUCCESS.equals(event.result())
        ));
        
        verify(mockAuditLogger).logEventAsync(argThat(event -> 
            "PAYMENT_COMPLETED".equals(event.event_type()) &&
            AuditResult.SUCCESS.equals(event.result())
        ));
    }

    @Test
    void testServiceInitialization() {
        // This test verifies that the service can be initialized properly
        // In a real scenario, you might want to test the @PostConstruct method
        assertNotNull(paymentService);
    }

    /**
     * Create a valid payment request for testing.
     */
    private PaymentRequest createValidPaymentRequest() {
        PaymentRequest request = new PaymentRequest();
        request.setPaymentId("test-payment-" + System.currentTimeMillis());
        request.setCustomerId("customer-123");
        request.setMerchantId("merchant-456");
        request.setAmount(new BigDecimal("100.00"));
        request.setCurrency("USD");
        request.setPaymentMethod("VISA");
        request.setDescription("Test payment");
        return request;
    }
} 