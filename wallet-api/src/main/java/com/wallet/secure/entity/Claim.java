package com.wallet.secure.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Entidad que representa una reclamación o siniestro asociado a un seguro.
 * <p>
 * Registra los detalles del incidente, su estado de resolución y el coste estimado.
 * </p>
 */
@Entity
@Table(name = "claims")
public class Claim {

    /**
     * Identificador único de la reclamación.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;
    
    private LocalDate incidentDate;
    
    @Enumerated(EnumType.STRING)
    private Status status; // OPEN, CLOSED, REJECTED
    
    private Double estimatedCost;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "insurance_id", nullable = false)
    @JsonIgnore
    private Insurance insurance;

    public enum Status {
        OPEN, CLOSED, REJECTED
    }

    public Claim() {
        this.status = Status.OPEN; // Default
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getIncidentDate() { return incidentDate; }
    public void setIncidentDate(LocalDate incidentDate) { this.incidentDate = incidentDate; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public Double getEstimatedCost() { return estimatedCost; }
    public void setEstimatedCost(Double estimatedCost) { this.estimatedCost = estimatedCost; }

    public Insurance getInsurance() { return insurance; }
    public void setInsurance(Insurance insurance) { this.insurance = insurance; }
}
