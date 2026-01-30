package com.wallet.secure.dto;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * DTO que representa una póliza de seguro en la capa de presentación.
 * <p>
 * Contiene validaciones para los formularios de creación y edición,
 * así como relaciones con los listados de reclamaciones, pagos y beneficiarios.
 * </p>
 */
public class Insurance {

    private Long id;

    @NotBlank(message = "El título es obligatorio")
    private String title;
    
    @NotBlank(message = "La compañía es obligatoria")
    private String company;
    private String policyNumber;
    @NotNull(message = "La categoría es obligatoria")
    private String category;
    
    @NotNull(message = "La fecha es obligatoria")
    @Future(message = "La fecha debe ser futura")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;
    
    // Missing StartDate compared to API? API had setStartDate. Check API entity if needed aside. 
    // Assuming this file reflects what was in 'secure'. 
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate; // Added just in case, API controller referenced it.

    @Pattern(regexp = "[679][0-9]{8}", message = "El teléfono debe tener 9 dígitos y empezar por 6, 7 o 9")
    private String phoneNumber;

    private String imageUrl;

    @NotNull(message = "El precio es obligatorio")
    @Min(value = 1, message = "El precio debe ser mayor a 0")
    private Double premiumAmount;
    
    @NotNull(message = "El periodo de pago es obligatorio")
    private PaymentPeriod paymentPeriod;

    @JsonIgnore
    private User user;

    public enum PaymentPeriod {
        MONTHLY, QUARTERLY, YEARLY
    }

    private java.util.List<Claim> claims = new java.util.ArrayList<>();

    private java.util.List<Payment> payments = new java.util.ArrayList<>();

    private java.util.List<Beneficiary> beneficiaries = new java.util.ArrayList<>();

    // Constructors
    public Insurance() {
        this.paymentPeriod = PaymentPeriod.MONTHLY; // Default
    }
    
    public java.util.List<Claim> getClaims() { return claims; }
    public void setClaims(java.util.List<Claim> claims) { this.claims = claims; }

    public java.util.List<Payment> getPayments() { return payments; }
    public void setPayments(java.util.List<Payment> payments) { this.payments = payments; }

    public java.util.List<Beneficiary> getBeneficiaries() { return beneficiaries; }
    public void setBeneficiaries(java.util.List<Beneficiary> beneficiaries) { this.beneficiaries = beneficiaries; }

    // Helper Method
    public boolean isActive() {
        return expiryDate != null && !expiryDate.isBefore(LocalDate.now());
    }

    public boolean isExpiringSoon() {
        if (expiryDate == null) return false;
        LocalDate now = LocalDate.now();
        return !expiryDate.isBefore(now) && expiryDate.isBefore(now.plusDays(30));
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getPolicyNumber() { return policyNumber; }
    public void setPolicyNumber(String policyNumber) { this.policyNumber = policyNumber; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Double getPremiumAmount() { return premiumAmount; }
    public void setPremiumAmount(Double premiumAmount) { this.premiumAmount = premiumAmount; }

    public PaymentPeriod getPaymentPeriod() { return paymentPeriod; }
    public void setPaymentPeriod(PaymentPeriod paymentPeriod) { this.paymentPeriod = paymentPeriod; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
