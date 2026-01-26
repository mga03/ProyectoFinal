package com.wallet.secure.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;

public class Payment {

    private Long id;

    private Double amount;
    private LocalDate paymentDate;
    
    private Status status; // PAID, PENDING, OVERDUE

    @JsonIgnore
    private Insurance insurance;

    public enum Status {
        PAID, PENDING, OVERDUE
    }

    public Payment() {}

    public Payment(Double amount, LocalDate paymentDate, Status status, Insurance insurance) {
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.status = status;
        this.insurance = insurance;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public Insurance getInsurance() { return insurance; }
    public void setInsurance(Insurance insurance) { this.insurance = insurance; }
}
