package com.enterprise.payment.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment response model.
 * Represents the result of a payment processing operation.
 */
public class PaymentResponse {

    @JsonProperty("payment_id")
    private String paymentId;

    @JsonProperty("transaction_id")
    private String transactionId;

    @JsonProperty("status")
    private PaymentStatus status;

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

    // Constructor with essential fields
    public PaymentResponse(String paymentId, PaymentStatus status, String transactionId, 
                          BigDecimal amount, String currency) {
        this.paymentId = paymentId;
        this.status = status;
        this.transactionId = transactionId;
        this.amount = amount;
        this.currency = currency;
        this.responseTimestamp = LocalDateTime.now();
    }

    // Constructor with all fields
    public PaymentResponse(String paymentId, PaymentStatus status, String transactionId, 
                          BigDecimal amount, String currency, BigDecimal processingFee, 
                          String message) {
        this.paymentId = paymentId;
        this.status = status;
        this.transactionId = transactionId;
        this.amount = amount;
        this.currency = currency;
        this.processingFee = processingFee;
        this.message = message;
        this.responseTimestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
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
                ", transactionId='" + transactionId + '\'' +
                ", status=" + status +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", processingFee=" + processingFee +
                ", message='" + message + '\'' +
                ", responseTimestamp=" + responseTimestamp +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PaymentResponse that = (PaymentResponse) o;

        if (paymentId != null ? !paymentId.equals(that.paymentId) : that.paymentId != null) return false;
        if (transactionId != null ? !transactionId.equals(that.transactionId) : that.transactionId != null) return false;
        if (status != that.status) return false;
        if (amount != null ? !amount.equals(that.amount) : that.amount != null) return false;
        if (currency != null ? !currency.equals(that.currency) : that.currency != null) return false;
        if (processingFee != null ? !processingFee.equals(that.processingFee) : that.processingFee != null) return false;
        if (message != null ? !message.equals(that.message) : that.message != null) return false;
        return responseTimestamp != null ? responseTimestamp.equals(that.responseTimestamp) : that.responseTimestamp == null;
    }

    @Override
    public int hashCode() {
        int result = paymentId != null ? paymentId.hashCode() : 0;
        result = 31 * result + (transactionId != null ? transactionId.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (amount != null ? amount.hashCode() : 0);
        result = 31 * result + (currency != null ? currency.hashCode() : 0);
        result = 31 * result + (processingFee != null ? processingFee.hashCode() : 0);
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + (responseTimestamp != null ? responseTimestamp.hashCode() : 0);
        return result;
    }
} 