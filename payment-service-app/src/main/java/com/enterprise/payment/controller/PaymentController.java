package com.enterprise.payment.controller;

import com.enterprise.payment.model.PaymentRequest;
import com.enterprise.payment.model.PaymentResponse;
import com.enterprise.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for payment operations.
 * Provides endpoints for processing payments and retrieving payment status.
 */
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    
    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Process a payment request.
     * 
     * @param request the payment request
     * @return the payment response
     */
    @PostMapping("/process")
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequest request) {
        logger.info("Received payment request: {}", request.getPaymentId());
        
        try {
            PaymentResponse response = paymentService.processPayment(request);
            
            HttpStatus status = switch (response.getStatus()) {
                case COMPLETED -> HttpStatus.OK;
                case PENDING -> HttpStatus.ACCEPTED;
                case DECLINED -> HttpStatus.BAD_REQUEST;
                case FAILED -> HttpStatus.INTERNAL_SERVER_ERROR;
                default -> HttpStatus.OK;
            };
            
            return ResponseEntity.status(status).body(response);
            
        } catch (Exception e) {
            logger.error("Error processing payment: {}", request.getPaymentId(), e);
            
            PaymentResponse errorResponse = new PaymentResponse();
            errorResponse.setPaymentId(request.getPaymentId());
            errorResponse.setStatus(com.enterprise.payment.model.PaymentStatus.FAILED);
            errorResponse.setMessage("Payment processing failed: " + e.getMessage());
            errorResponse.setAmount(request.getAmount());
            errorResponse.setCurrency(request.getCurrency());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get payment status by payment ID.
     * 
     * @param paymentId the payment ID
     * @return the payment response
     */
    @GetMapping("/{paymentId}/status")
    public ResponseEntity<PaymentResponse> getPaymentStatus(@PathVariable String paymentId) {
        logger.info("Requesting payment status: {}", paymentId);
        
        try {
            PaymentResponse response = paymentService.getPaymentStatus(paymentId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error retrieving payment status: {}", paymentId, e);
            
            PaymentResponse errorResponse = new PaymentResponse();
            errorResponse.setPaymentId(paymentId);
            errorResponse.setStatus(com.enterprise.payment.model.PaymentStatus.FAILED);
            errorResponse.setMessage("Failed to retrieve payment status: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Health check endpoint.
     * 
     * @return health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "payment-service");
        health.put("version", "2.0.0");
        health.put("java", System.getProperty("java.version"));
        
        return ResponseEntity.ok(health);
    }

    /**
     * Exception handler for validation errors.
     * 
     * @param e the validation exception
     * @return error response
     */
    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            jakarta.validation.ConstraintViolationException e) {
        
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Validation failed");
        error.put("message", e.getMessage());
        error.put("status", "BAD_REQUEST");
        
        logger.warn("Validation error: {}", e.getMessage());
        
        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Generic exception handler.
     * 
     * @param e the exception
     * @return error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception e) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Internal server error");
        error.put("message", e.getMessage());
        error.put("status", "INTERNAL_SERVER_ERROR");
        
        logger.error("Unhandled exception", e);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
} 