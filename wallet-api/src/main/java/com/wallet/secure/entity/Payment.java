package com.wallet.secure.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Entidad que representa un pago realizado asociado a una póliza de seguro.
 * <p>
 * Almacena la cantidad, la fecha de pago y el estado de la transacción.
 * </p>
 */
@Entity
@Table(name = "payments")
public class Payment {

    /**
     * Identificador único del pago.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double amount;
    private LocalDate paymentDate;
    
    @Enumerated(EnumType.STRING)
    private Status status; // PAID, PENDING, OVERDUE

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insurance_id")
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
