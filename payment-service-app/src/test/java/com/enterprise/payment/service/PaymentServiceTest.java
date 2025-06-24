package com.enterprise.payment.service;

import com.enterprise.audit.logging.model.AuditContext;
import com.enterprise.audit.logging.model.AuditResult;
import com.enterprise.audit.logging.service.AuditLogger;
import com.enterprise.audit.logging.service.FileSystemAuditLogger;
import com.enterprise.payment.model.PaymentRequest;
import com.enterprise.payment.model.PaymentResponse;
import com.enterprise.payment.model.PaymentStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PaymentService with audit logging verification.
 */
@RunWith(MockitoJUnitRunner.class)
public class PaymentServiceTest {

    @Mock
    private AuditLogger mockAuditLogger;

    private PaymentService paymentService;

    @Before
    public void setUp() {
        paymentService = new PaymentService();
        
        // Use reflection to inject the mock audit logger
        try {
            java.lang.reflect.Field auditLoggerField = PaymentService.class.getDeclaredField("auditLogger");
            auditLoggerField.setAccessible(true);
            auditLoggerField.set(paymentService, mockAuditLogger);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject mock audit logger", e);
        }
    }

    @Test
    public void testProcessPayment_Success() throws Exception {
        // Arrange
        PaymentRequest request = createValidPaymentRequest();
        request.setAmount(new BigDecimal("100.00"));
        
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
        
        // Verify audit logging
        verify(mockAuditLogger, times(1)).logEvent(
            eq("PAYMENT_INITIATED"), 
            eq("process_payment"), 
            eq("payment/" + request.getPaymentId()), 
            eq(AuditResult.SUCCESS), 
            eq("Payment processing initiated"), 
            any()
        );
        
        verify(mockAuditLogger, times(1)).logSuccess(
            eq("PAYMENT_COMPLETED"), 
            eq("process_payment"), 
            eq("payment/" + request.getPaymentId()), 
            eq("Payment processed successfully")
        );
    }

    @Test
    public void testProcessPayment_DeclinedDueToLargeAmount() throws Exception {
        // Arrange
        PaymentRequest request = createValidPaymentRequest();
        request.setAmount(new BigDecimal("15000.00")); // Exceeds limit
        
        // Act
        PaymentResponse response = paymentService.processPayment(request);
        
        // Assert
        assertNotNull(response);
        assertEquals(PaymentStatus.DECLINED, response.getStatus());
        assertTrue(response.getMessage().contains("Amount exceeds limit"));
        
        // Verify audit logging
        verify(mockAuditLogger, times(1)).logEvent(
            eq("PAYMENT_INITIATED"), 
            eq("process_payment"), 
            eq("payment/" + request.getPaymentId()), 
            eq(AuditResult.SUCCESS), 
            eq("Payment processing initiated"), 
            any()
        );
        
        verify(mockAuditLogger, times(1)).logFailure(
            eq("PAYMENT_DECLINED"), 
            eq("process_payment"), 
            eq("payment/" + request.getPaymentId()), 
            contains("Amount exceeds limit")
        );
    }

    @Test
    public void testProcessPayment_DeclinedDueToInvalidPaymentMethod() throws Exception {
        // Arrange
        PaymentRequest request = createValidPaymentRequest();
        request.setPaymentMethod("INVALID_CARD");
        
        // Act
        PaymentResponse response = paymentService.processPayment(request);
        
        // Assert
        assertNotNull(response);
        assertEquals(PaymentStatus.DECLINED, response.getStatus());
        assertTrue(response.getMessage().contains("Invalid payment method"));
        
        // Verify audit logging
        verify(mockAuditLogger, times(1)).logFailure(
            eq("PAYMENT_DECLINED"), 
            eq("process_payment"), 
            eq("payment/" + request.getPaymentId()), 
            contains("Invalid payment method")
        );
    }

    @Test
    public void testProcessPayment_FailedDueToSmallAmount() throws Exception {
        // Arrange
        PaymentRequest request = createValidPaymentRequest();
        request.setAmount(new BigDecimal("0.005")); // Too small
        
        // Act
        PaymentResponse response = paymentService.processPayment(request);
        
        // Assert
        assertNotNull(response);
        assertEquals(PaymentStatus.FAILED, response.getStatus());
        assertTrue(response.getMessage().contains("Amount too small"));
        
        // Verify audit logging
        verify(mockAuditLogger, times(1)).logEvent(
            eq("PAYMENT_PROCESSED"), 
            eq("process_payment"), 
            eq("payment/" + request.getPaymentId()), 
            eq(AuditResult.SUCCESS), 
            contains("FAILED"), 
            any()
        );
    }

    @Test
    public void testProcessPayment_WithAuditContext() throws Exception {
        // Arrange
        PaymentRequest request = createValidPaymentRequest();
        String correlationId = "test-correlation-id";
        String userId = "test-user";
        String sessionId = "test-session";
        
        AuditContext.setCorrelationId(correlationId);
        AuditContext.setUserId(userId);
        AuditContext.setSessionId(sessionId);
        
        // Act
        PaymentResponse response = paymentService.processPayment(request);
        
        // Assert
        assertNotNull(response);
        assertEquals(PaymentStatus.COMPLETED, response.getStatus());
        
        // Verify audit logging with context
        verify(mockAuditLogger, times(1)).logEvent(
            eq("PAYMENT_INITIATED"), 
            eq("process_payment"), 
            eq("payment/" + request.getPaymentId()), 
            eq(AuditResult.SUCCESS), 
            eq("Payment processing initiated"), 
            any()
        );
        
        // Clean up
        AuditContext.setCorrelationId(null);
        AuditContext.setUserId(null);
        AuditContext.setSessionId(null);
    }

    @Test
    public void testProcessPayment_ExceptionHandling() throws Exception {
        // Arrange
        PaymentRequest request = createValidPaymentRequest();
        
        // Mock audit logger to throw exception
        doThrow(new RuntimeException("Audit logging failed"))
            .when(mockAuditLogger).logEvent(anyString(), anyString(), anyString(), any(), anyString(), any());
        
        // Act
        PaymentResponse response = paymentService.processPayment(request);
        
        // Assert
        assertNotNull(response);
        assertEquals(PaymentStatus.FAILED, response.getStatus());
        assertTrue(response.getMessage().contains("Payment processing failed"));
        
        // Verify error logging
        verify(mockAuditLogger, times(1)).logFailure(
            eq("PAYMENT_ERROR"), 
            eq("process_payment"), 
            eq("payment/" + request.getPaymentId()), 
            contains("Payment processing failed")
        );
    }

    @Test
    public void testGetPaymentStatus_Success() throws Exception {
        // Arrange
        String paymentId = "test-payment-123";
        
        // Act
        PaymentResponse response = paymentService.getPaymentStatus(paymentId);
        
        // Assert
        assertNotNull(response);
        assertEquals(paymentId, response.getPaymentId());
        assertEquals(PaymentStatus.COMPLETED, response.getStatus());
        assertNotNull(response.getTransactionId());
        assertEquals("USD", response.getCurrency());
        
        // Verify audit logging
        verify(mockAuditLogger, times(1)).logEvent(
            eq("PAYMENT_STATUS_REQUESTED"), 
            eq("get_status"), 
            eq("payment/" + paymentId), 
            eq(AuditResult.SUCCESS), 
            eq("Payment status requested")
        );
        
        verify(mockAuditLogger, times(1)).logSuccess(
            eq("PAYMENT_STATUS_RETRIEVED"), 
            eq("get_status"), 
            eq("payment/" + paymentId), 
            eq("Payment status retrieved successfully")
        );
    }

    @Test
    public void testGetPaymentStatus_ExceptionHandling() throws Exception {
        // Arrange
        String paymentId = "test-payment-123";
        
        // Mock audit logger to throw exception
        doThrow(new RuntimeException("Audit logging failed"))
            .when(mockAuditLogger).logEvent(anyString(), anyString(), anyString(), any(), anyString());
        
        // Act & Assert
        try {
            paymentService.getPaymentStatus(paymentId);
            fail("Expected exception to be thrown");
        } catch (RuntimeException e) {
            // Expected
        }
        
        // Verify error logging
        verify(mockAuditLogger, times(1)).logFailure(
            eq("PAYMENT_STATUS_ERROR"), 
            eq("get_status"), 
            eq("payment/" + paymentId), 
            contains("Failed to retrieve payment status")
        );
    }

    @Test
    public void testProcessingFeeCalculation() throws Exception {
        // Test minimum fee for small amounts
        PaymentRequest smallRequest = createValidPaymentRequest();
        smallRequest.setAmount(new BigDecimal("1.00"));
        PaymentResponse smallResponse = paymentService.processPayment(smallRequest);
        assertEquals(new BigDecimal("0.30"), smallResponse.getProcessingFee());
        
        // Test percentage fee for larger amounts
        PaymentRequest largeRequest = createValidPaymentRequest();
        largeRequest.setAmount(new BigDecimal("1000.00"));
        PaymentResponse largeResponse = paymentService.processPayment(largeRequest);
        assertEquals(new BigDecimal("29.00"), largeResponse.getProcessingFee());
    }

    /**
     * Create a valid payment request for testing.
     */
    private PaymentRequest createValidPaymentRequest() {
        PaymentRequest request = new PaymentRequest();
        request.setPaymentId("test-payment-" + System.currentTimeMillis());
        request.setMerchantId("merchant-123");
        request.setCustomerId("customer-456");
        request.setAmount(new BigDecimal("100.00"));
        request.setCurrency("USD");
        request.setPaymentMethod("CREDIT_CARD");
        request.setDescription("Test payment");
        return request;
    }
} 