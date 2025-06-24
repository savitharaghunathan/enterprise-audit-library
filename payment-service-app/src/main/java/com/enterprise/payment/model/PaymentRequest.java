package com.enterprise.payment.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a payment processing request.
 */
public class PaymentRequest {

    @JsonProperty("payment_id")
    @NotBlank(message = "Payment ID is required")
    @Size(max = 50, message = "Payment ID must not exceed 50 characters")
    private String paymentId;

    @JsonProperty("merchant_id")
    @NotBlank(message = "Merchant ID is required")
    @Size(max = 50, message = "Merchant ID must not exceed 50 characters")
    private String merchantId;

    @JsonProperty("customer_id")
    @NotBlank(message = "Customer ID is required")
    @Size(max = 50, message = "Customer ID must not exceed 50 characters")
    private String customerId;

    @JsonProperty("amount")
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    private BigDecimal amount;

    @JsonProperty("currency")
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
    private String currency;

    @JsonProperty("payment_method")
    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    @JsonProperty("description")
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @JsonProperty("request_timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime requestTimestamp;

    // Default constructor
    public PaymentRequest() {
        this.requestTimestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
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

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getRequestTimestamp() {
        return requestTimestamp;
    }

    public void setRequestTimestamp(LocalDateTime requestTimestamp) {
        this.requestTimestamp = requestTimestamp;
    }

    @Override
    public String toString() {
        return "PaymentRequest{" +
                "paymentId='" + paymentId + '\'' +
                ", merchantId='" + merchantId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", description='" + description + '\'' +
                ", requestTimestamp=" + requestTimestamp +
                '}';
    }
} 