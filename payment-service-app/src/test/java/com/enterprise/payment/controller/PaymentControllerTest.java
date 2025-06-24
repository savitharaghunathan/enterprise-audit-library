package com.enterprise.payment.controller;

import com.enterprise.payment.model.PaymentRequest;
import com.enterprise.payment.model.PaymentResponse;
import com.enterprise.payment.model.PaymentStatus;
import com.enterprise.payment.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for PaymentController using Spring Boot Test.
 * Tests REST API endpoints with comprehensive validation and error handling.
 */
@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    private PaymentRequest validPaymentRequest;
    private PaymentResponse validPaymentResponse;

    @BeforeEach
    void setUp() {
        // Create a valid payment request
        validPaymentRequest = new PaymentRequest();
        validPaymentRequest.setPaymentId("test-payment-123");
        validPaymentRequest.setCustomerId("customer-123");
        validPaymentRequest.setMerchantId("merchant-456");
        validPaymentRequest.setAmount(new BigDecimal("100.00"));
        validPaymentRequest.setCurrency("USD");
        validPaymentRequest.setPaymentMethod("VISA");
        validPaymentRequest.setDescription("Test payment");

        // Create a valid payment response
        validPaymentResponse = new PaymentResponse();
        validPaymentResponse.setPaymentId("test-payment-123");
        validPaymentResponse.setStatus(PaymentStatus.COMPLETED);
        validPaymentResponse.setTransactionId("TXN-ABC12345");
        validPaymentResponse.setAmount(new BigDecimal("100.00"));
        validPaymentResponse.setCurrency("USD");
        validPaymentResponse.setProcessingFee(new BigDecimal("2.90"));
        validPaymentResponse.setMessage("Payment processed successfully");
    }

    @Test
    void testProcessPayment_Success() throws Exception {
        // Arrange
        when(paymentService.processPayment(any(PaymentRequest.class)))
            .thenReturn(validPaymentResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/payments/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validPaymentRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.payment_id").value("test-payment-123"))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.transaction_id").value("TXN-ABC12345"))
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.processing_fee").value(2.90))
                .andExpect(jsonPath("$.message").value("Payment processed successfully"));
    }

    @Test
    void testProcessPayment_Declined_ReturnsBadRequest() throws Exception {
        // Arrange
        PaymentResponse declinedResponse = new PaymentResponse();
        declinedResponse.setPaymentId("test-payment-123");
        declinedResponse.setStatus(PaymentStatus.DECLINED);
        declinedResponse.setMessage("Payment declined: Amount exceeds limit");
        declinedResponse.setAmount(new BigDecimal("15000.00"));
        declinedResponse.setCurrency("USD");

        when(paymentService.processPayment(any(PaymentRequest.class)))
            .thenReturn(declinedResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/payments/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validPaymentRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("DECLINED"))
                .andExpect(jsonPath("$.message").value("Payment declined: Amount exceeds limit"));
    }

    @Test
    void testProcessPayment_Failed_ReturnsInternalServerError() throws Exception {
        // Arrange
        PaymentResponse failedResponse = new PaymentResponse();
        failedResponse.setPaymentId("test-payment-123");
        failedResponse.setStatus(PaymentStatus.FAILED);
        failedResponse.setMessage("Payment processing failed");
        failedResponse.setAmount(new BigDecimal("100.00"));
        failedResponse.setCurrency("USD");

        when(paymentService.processPayment(any(PaymentRequest.class)))
            .thenReturn(failedResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/payments/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validPaymentRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.message").value("Payment processing failed"));
    }

    @Test
    void testProcessPayment_ValidationError_MissingPaymentId() throws Exception {
        // Arrange
        PaymentRequest invalidRequest = new PaymentRequest();
        invalidRequest.setCustomerId("customer-123");
        invalidRequest.setMerchantId("merchant-456");
        invalidRequest.setAmount(new BigDecimal("100.00"));
        invalidRequest.setCurrency("USD");
        invalidRequest.setPaymentMethod("VISA");

        // Act & Assert
        mockMvc.perform(post("/api/v1/payments/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testProcessPayment_ValidationError_InvalidAmount() throws Exception {
        // Arrange
        PaymentRequest invalidRequest = new PaymentRequest();
        invalidRequest.setPaymentId("test-payment-123");
        invalidRequest.setCustomerId("customer-123");
        invalidRequest.setMerchantId("merchant-456");
        invalidRequest.setAmount(new BigDecimal("-10.00")); // Negative amount
        invalidRequest.setCurrency("USD");
        invalidRequest.setPaymentMethod("VISA");

        // Act & Assert
        mockMvc.perform(post("/api/v1/payments/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testProcessPayment_ValidationError_InvalidCurrency() throws Exception {
        // Arrange
        PaymentRequest invalidRequest = new PaymentRequest();
        invalidRequest.setPaymentId("test-payment-123");
        invalidRequest.setCustomerId("customer-123");
        invalidRequest.setMerchantId("merchant-456");
        invalidRequest.setAmount(new BigDecimal("100.00"));
        invalidRequest.setCurrency("INVALID"); // Invalid currency code
        invalidRequest.setPaymentMethod("VISA");

        // Act & Assert
        mockMvc.perform(post("/api/v1/payments/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testProcessPayment_ServiceException_ReturnsInternalServerError() throws Exception {
        // Arrange
        when(paymentService.processPayment(any(PaymentRequest.class)))
            .thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/payments/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validPaymentRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.message").value("Payment processing failed: Service error"));
    }

    @Test
    void testGetPaymentStatus_Success() throws Exception {
        // Arrange
        when(paymentService.getPaymentStatus("test-payment-123"))
            .thenReturn(validPaymentResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/payments/test-payment-123/status"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.payment_id").value("test-payment-123"))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.transaction_id").value("TXN-ABC12345"));
    }

    @Test
    void testGetPaymentStatus_ServiceException_ReturnsInternalServerError() throws Exception {
        // Arrange
        when(paymentService.getPaymentStatus("test-payment-123"))
            .thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/payments/test-payment-123/status"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.message").value("Failed to retrieve payment status: Service error"));
    }

    @Test
    void testHealthCheck_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/payments/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("payment-service"))
                .andExpect(jsonPath("$.version").value("2.0.0"))
                .andExpect(jsonPath("$.java").exists());
    }

    @Test
    void testProcessPayment_InvalidJson_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/payments/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content("invalid json content"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testProcessPayment_EmptyBody_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/payments/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testProcessPayment_PendingStatus_ReturnsAccepted() throws Exception {
        // Arrange
        PaymentResponse pendingResponse = new PaymentResponse();
        pendingResponse.setPaymentId("test-payment-123");
        pendingResponse.setStatus(PaymentStatus.PENDING);
        pendingResponse.setMessage("Payment is being processed");
        pendingResponse.setAmount(new BigDecimal("100.00"));
        pendingResponse.setCurrency("USD");

        when(paymentService.processPayment(any(PaymentRequest.class)))
            .thenReturn(pendingResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/payments/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validPaymentRequest)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }
} 