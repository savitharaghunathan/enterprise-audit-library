package com.enterprise.payment.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response from a payment gateway (Stripe, PayPal, etc.)
 * Contains the actual response from the bank/payment network
 */
public class GatewayResponse {
    
    @JsonProperty("success")
    private boolean success;
    
    @JsonProperty("gateway_transaction_id")
    private String gatewayTransactionId;
    
    @JsonProperty("gateway_response_code")
    private String gatewayResponseCode;
    
    @JsonProperty("gateway_response_message")
    private String gatewayResponseMessage;
    
    @JsonProperty("authorization_code")
    private String authorizationCode;
    
    @JsonProperty("avs_result")
    private String avsResult; // Address Verification System result
    
    @JsonProperty("cvv_result")
    private String cvvResult; // CVV verification result
    
    @JsonProperty("amount_authorized")
    private BigDecimal amountAuthorized;
    
    @JsonProperty("amount_captured")
    private BigDecimal amountCaptured;
    
    @JsonProperty("currency")
    private String currency;
    
    @JsonProperty("processing_time_ms")
    private Long processingTimeMs;
    
    @JsonProperty("gateway_fee")
    private BigDecimal gatewayFee;
    
    @JsonProperty("gateway_timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime gatewayTimestamp;
    
    @JsonProperty("risk_score")
    private Integer riskScore; // 0-100, higher = more risky
    
    @JsonProperty("fraud_indicators")
    private String[] fraudIndicators;
    
    // Default constructor
    public GatewayResponse() {}
    
    // Constructor for successful response
    public GatewayResponse(boolean success, String gatewayTransactionId, String gatewayResponseCode) {
        this.success = success;
        this.gatewayTransactionId = gatewayTransactionId;
        this.gatewayResponseCode = gatewayResponseCode;
        this.gatewayTimestamp = LocalDateTime.now();
    }
    
    // Getters and setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getGatewayTransactionId() {
        return gatewayTransactionId;
    }
    
    public void setGatewayTransactionId(String gatewayTransactionId) {
        this.gatewayTransactionId = gatewayTransactionId;
    }
    
    public String getGatewayResponseCode() {
        return gatewayResponseCode;
    }
    
    public void setGatewayResponseCode(String gatewayResponseCode) {
        this.gatewayResponseCode = gatewayResponseCode;
    }
    
    public String getGatewayResponseMessage() {
        return gatewayResponseMessage;
    }
    
    public void setGatewayResponseMessage(String gatewayResponseMessage) {
        this.gatewayResponseMessage = gatewayResponseMessage;
    }
    
    public String getAuthorizationCode() {
        return authorizationCode;
    }
    
    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }
    
    public String getAvsResult() {
        return avsResult;
    }
    
    public void setAvsResult(String avsResult) {
        this.avsResult = avsResult;
    }
    
    public String getCvvResult() {
        return cvvResult;
    }
    
    public void setCvvResult(String cvvResult) {
        this.cvvResult = cvvResult;
    }
    
    public BigDecimal getAmountAuthorized() {
        return amountAuthorized;
    }
    
    public void setAmountAuthorized(BigDecimal amountAuthorized) {
        this.amountAuthorized = amountAuthorized;
    }
    
    public BigDecimal getAmountCaptured() {
        return amountCaptured;
    }
    
    public void setAmountCaptured(BigDecimal amountCaptured) {
        this.amountCaptured = amountCaptured;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public Long getProcessingTimeMs() {
        return processingTimeMs;
    }
    
    public void setProcessingTimeMs(Long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }
    
    public BigDecimal getGatewayFee() {
        return gatewayFee;
    }
    
    public void setGatewayFee(BigDecimal gatewayFee) {
        this.gatewayFee = gatewayFee;
    }
    
    public LocalDateTime getGatewayTimestamp() {
        return gatewayTimestamp;
    }
    
    public void setGatewayTimestamp(LocalDateTime gatewayTimestamp) {
        this.gatewayTimestamp = gatewayTimestamp;
    }
    
    public Integer getRiskScore() {
        return riskScore;
    }
    
    public void setRiskScore(Integer riskScore) {
        this.riskScore = riskScore;
    }
    
    public String[] getFraudIndicators() {
        return fraudIndicators;
    }
    
    public void setFraudIndicators(String[] fraudIndicators) {
        this.fraudIndicators = fraudIndicators;
    }
    
    @Override
    public String toString() {
        return "GatewayResponse{" +
                "success=" + success +
                ", gatewayTransactionId='" + gatewayTransactionId + '\'' +
                ", gatewayResponseCode='" + gatewayResponseCode + '\'' +
                ", gatewayResponseMessage='" + gatewayResponseMessage + '\'' +
                ", authorizationCode='" + authorizationCode + '\'' +
                ", avsResult='" + avsResult + '\'' +
                ", cvvResult='" + cvvResult + '\'' +
                ", amountAuthorized=" + amountAuthorized +
                ", amountCaptured=" + amountCaptured +
                ", currency='" + currency + '\'' +
                ", processingTimeMs=" + processingTimeMs +
                ", gatewayFee=" + gatewayFee +
                ", gatewayTimestamp=" + gatewayTimestamp +
                ", riskScore=" + riskScore +
                '}';
    }
} 