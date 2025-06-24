package com.enterprise.payment.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * Billing address information required by payment gateways
 * for fraud prevention and compliance (PCI DSS, etc.)
 */
public class BillingAddress {
    
    @JsonProperty("line1")
    @NotBlank(message = "Address line 1 is required")
    @Size(max = 100, message = "Address line 1 must not exceed 100 characters")
    private String line1;
    
    @JsonProperty("line2")
    @Size(max = 100, message = "Address line 2 must not exceed 100 characters")
    private String line2;
    
    @JsonProperty("city")
    @NotBlank(message = "City is required")
    @Size(max = 50, message = "City must not exceed 50 characters")
    private String city;
    
    @JsonProperty("state")
    @NotBlank(message = "State/province is required")
    @Size(max = 50, message = "State/province must not exceed 50 characters")
    private String state;
    
    @JsonProperty("postal_code")
    @NotBlank(message = "Postal code is required")
    @Pattern(regexp = "^[0-9A-Za-z\\s-]{3,10}$", message = "Postal code must be 3-10 characters")
    private String postalCode;
    
    @JsonProperty("country")
    @NotBlank(message = "Country is required")
    @Pattern(regexp = "^[A-Z]{2}$", message = "Country must be 2-letter ISO code")
    private String country;
    
    // Default constructor
    public BillingAddress() {}
    
    // Constructor with all fields
    public BillingAddress(String line1, String line2, String city, String state, String postalCode, String country) {
        this.line1 = line1;
        this.line2 = line2;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.country = country;
    }
    
    // Getters and setters
    public String getLine1() {
        return line1;
    }
    
    public void setLine1(String line1) {
        this.line1 = line1;
    }
    
    public String getLine2() {
        return line2;
    }
    
    public void setLine2(String line2) {
        this.line2 = line2;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
    
    public String getPostalCode() {
        return postalCode;
    }
    
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    @Override
    public String toString() {
        return "BillingAddress{" +
                "line1='" + line1 + '\'' +
                ", line2='" + line2 + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", postalCode='" + postalCode + '\'' +
                ", country='" + country + '\'' +
                '}';
    }
} 