package com.enterprise.payment.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a payment processing response.
 */
public class PaymentResponse {

    @JsonProperty("payment_id")
    private String paymentId;

    @JsonProperty("status")
    private PaymentStatus status;

    @JsonProperty("transaction_id")
    private String transactionId;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("processing_fee")
    private BigDecimal processingFee;

    @JsonProperty("message")
    private String message;

    @JsonProperty("response_timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime responseTimestamp;

    // Default constructor
    public PaymentResponse() {
        this.responseTimestamp = LocalDateTime.now();
    }

    // Constructor with required fields
    public PaymentResponse(String paymentId, PaymentStatus status, String transactionId, 
                          BigDecimal amount, String currency) {
        this();
        this.paymentId = paymentId;
        this.status = status;
        this.transactionId = transactionId;
        this.amount = amount;
        this.currency = currency;
    }

    // Getters and Setters
    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getProcessingFee() {
        return processingFee;
    }

    public void setProcessingFee(BigDecimal processingFee) {
        this.processingFee = processingFee;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getResponseTimestamp() {
        return responseTimestamp;
    }

    public void setResponseTimestamp(LocalDateTime responseTimestamp) {
        this.responseTimestamp = responseTimestamp;
    }

    @Override
    public String toString() {
        return "PaymentResponse{" +
                "paymentId='" + paymentId + '\'' +
                ", status=" + status +
                ", transactionId='" + transactionId + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", processingFee=" + processingFee +
                ", message='" + message + '\'' +
                ", responseTimestamp=" + responseTimestamp +
                '}';
    }
} 