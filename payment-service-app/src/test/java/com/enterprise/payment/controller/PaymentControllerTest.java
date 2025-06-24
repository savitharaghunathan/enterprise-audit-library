package com.enterprise.payment.controller;

import com.enterprise.audit.logging.model.AuditContext;
import com.enterprise.audit.logging.model.AuditResult;
import com.enterprise.audit.logging.service.AuditLogger;
import com.enterprise.payment.model.PaymentRequest;
import com.enterprise.payment.model.PaymentResponse;
import com.enterprise.payment.model.PaymentStatus;
import com.enterprise.payment.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindingResult;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for PaymentController with audit logging verification.
 */
@RunWith(MockitoJUnitRunner.class)
public class PaymentControllerTest {

    @Mock
    private PaymentService mockPaymentService;

    @Mock
    private AuditLogger mockAuditLogger;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        
        PaymentController controller = new PaymentController();
        
        // Use reflection to inject the mock dependencies
        try {
            java.lang.reflect.Field paymentServiceField = PaymentController.class.getDeclaredField("paymentService");
            paymentServiceField.setAccessible(true);
            paymentServiceField.set(controller, mockPaymentService);
            
            java.lang.reflect.Field auditLoggerField = PaymentController.class.getDeclaredField("auditLogger");
            auditLoggerField.setAccessible(true);
            auditLoggerField.set(controller, mockAuditLogger);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject mock dependencies", e);
        }
        
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    public void testProcessPayment_Success() throws Exception {
        // Arrange
        PaymentRequest request = createValidPaymentRequest();
        PaymentResponse response = createSuccessfulPaymentResponse(request);
        
        when(mockPaymentService.processPayment(any(PaymentRequest.class))).thenReturn(response);
        
        // Act & Assert
        mockMvc.perform(post("/api/v1/payments/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payment_id").value(request.getPaymentId()))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.transaction_id").exists())
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.currency").value("USD"));
        
        // Verify audit logging
        verify(mockAuditLogger, times(1)).logEvent(
            eq("API_REQUEST"), 
            eq("process_payment"), 
            eq("api/payments/process"), 
            eq(AuditResult.SUCCESS), 
            eq("Payment processing API request received"), 
            any()
        );
        
        verify(mockAuditLogger, times(1)).logEvent(
            eq("API_RESPONSE"), 
            eq("process_payment"), 
            eq("api/payments/process"), 
            eq(AuditResult.SUCCESS), 
            eq("Payment processing API response sent"), 
            any()
        );
    }

    @Test
    public void testProcessPayment_ValidationError() throws Exception {
        // Arrange
        PaymentRequest request = createValidPaymentRequest();
        request.setAmount(null); // Invalid amount
        
        // Act & Assert
        mockMvc.perform(post("/api/v1/payments/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Amount is required")));
        
        // Verify audit logging
        verify(mockAuditLogger, times(1)).logEvent(
            eq("API_REQUEST"), 
            eq("process_payment"), 
            eq("api/payments/process"), 
            eq(AuditResult.SUCCESS), 
            eq("Payment processing API request received"), 
            any()
        );
        
        verify(mockAuditLogger, times(1)).logFailure(
            eq("VALIDATION_ERROR"), 
            eq("process_payment"), 
            eq("api/payments/process"), 
            contains("Amount is required")
        );
        
        // Verify service is not called
        verify(mockPaymentService, never()).processPayment(any(PaymentRequest.class));
    }

    @Test
    public void testProcessPayment_ServiceException() throws Exception {
        // Arrange
        PaymentRequest request = createValidPaymentRequest();
        
        when(mockPaymentService.processPayment(any(PaymentRequest.class)))
            .thenThrow(new RuntimeException("Service error"));
        
        // Act & Assert
        mockMvc.perform(post("/api/v1/payments/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Internal server error")));
        
        // Verify audit logging
        verify(mockAuditLogger, times(1)).logFailure(
            eq("API_ERROR"), 
            eq("process_payment"), 
            eq("api/payments/process"), 
            contains("API error")
        );
    }

    @Test
    public void testGetPaymentStatus_Success() throws Exception {
        // Arrange
        String paymentId = "test-payment-123";
        PaymentResponse response = new PaymentResponse();
        response.setPaymentId(paymentId);
        response.setStatus(PaymentStatus.COMPLETED);
        response.setTransactionId("TXN-12345678");
        response.setAmount(new BigDecimal("100.00"));
        response.setCurrency("USD");
        response.setMessage("Payment status retrieved");
        
        when(mockPaymentService.getPaymentStatus(paymentId)).thenReturn(response);
        
        // Act & Assert
        mockMvc.perform(get("/api/v1/payments/{paymentId}/status", paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payment_id").value(paymentId))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.transaction_id").value("TXN-12345678"));
        
        // Verify audit logging
        verify(mockAuditLogger, times(1)).logEvent(
            eq("API_REQUEST"), 
            eq("get_status"), 
            eq("api/payments/" + paymentId + "/status"), 
            eq(AuditResult.SUCCESS), 
            eq("Payment status API request received")
        );
        
        verify(mockAuditLogger, times(1)).logEvent(
            eq("API_RESPONSE"), 
            eq("get_status"), 
            eq("api/payments/" + paymentId + "/status"), 
            eq(AuditResult.SUCCESS), 
            eq("Payment status API response sent")
        );
    }

    @Test
    public void testGetPaymentStatus_ServiceException() throws Exception {
        // Arrange
        String paymentId = "test-payment-123";
        
        when(mockPaymentService.getPaymentStatus(paymentId))
            .thenThrow(new RuntimeException("Service error"));
        
        // Act & Assert
        mockMvc.perform(get("/api/v1/payments/{paymentId}/status", paymentId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Internal server error")));
        
        // Verify audit logging
        verify(mockAuditLogger, times(1)).logFailure(
            eq("API_ERROR"), 
            eq("get_status"), 
            eq("api/payments/" + paymentId + "/status"), 
            contains("API error")
        );
    }

    @Test
    public void testHealthCheck() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/payments/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("payment-service"))
                .andExpect(jsonPath("$.version").value("1.0.0"));
    }

    @Test
    public void testProcessPayment_WithClientIpAddress() throws Exception {
        // Arrange
        PaymentRequest request = createValidPaymentRequest();
        PaymentResponse response = createSuccessfulPaymentResponse(request);
        
        when(mockPaymentService.processPayment(any(PaymentRequest.class))).thenReturn(response);
        
        // Act & Assert
        mockMvc.perform(post("/api/v1/payments/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("X-Forwarded-For", "192.168.1.100")
                .header("User-Agent", "TestClient/1.0"))
                .andExpect(status().isOk());
        
        // Verify audit logging with client information
        verify(mockAuditLogger, times(1)).logEvent(
            eq("API_REQUEST"), 
            eq("process_payment"), 
            eq("api/payments/process"), 
            eq(AuditResult.SUCCESS), 
            eq("Payment processing API request received"), 
            any()
        );
    }

    @Test
    public void testProcessPayment_WithRealIpAddress() throws Exception {
        // Arrange
        PaymentRequest request = createValidPaymentRequest();
        PaymentResponse response = createSuccessfulPaymentResponse(request);
        
        when(mockPaymentService.processPayment(any(PaymentRequest.class))).thenReturn(response);
        
        // Act & Assert
        mockMvc.perform(post("/api/v1/payments/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("X-Real-IP", "10.0.0.50"))
                .andExpect(status().isOk());
        
        // Verify audit logging
        verify(mockAuditLogger, times(1)).logEvent(
            eq("API_REQUEST"), 
            eq("process_payment"), 
            eq("api/payments/process"), 
            eq(AuditResult.SUCCESS), 
            eq("Payment processing API request received"), 
            any()
        );
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

    /**
     * Create a successful payment response for testing.
     */
    private PaymentResponse createSuccessfulPaymentResponse(PaymentRequest request) {
        PaymentResponse response = new PaymentResponse();
        response.setPaymentId(request.getPaymentId());
        response.setStatus(PaymentStatus.COMPLETED);
        response.setTransactionId("TXN-" + System.currentTimeMillis());
        response.setAmount(request.getAmount());
        response.setCurrency(request.getCurrency());
        response.setProcessingFee(new BigDecimal("2.90"));
        response.setMessage("Payment processed successfully");
        return response;
    }
} 