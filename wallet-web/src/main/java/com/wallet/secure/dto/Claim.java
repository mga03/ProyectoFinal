package com.wallet.secure.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;

/**
 * DTO que representa una reclamación o siniestro asociado a un seguro.
 * Utilizado para la transferencia de datos entre el cliente (API) y las vistas.
 */
public class Claim {

    private Long id;

    private String description;
    
    private LocalDate incidentDate;
    
    private Status status; // OPEN, CLOSED, REJECTED
    
    private Double estimatedCost;

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
