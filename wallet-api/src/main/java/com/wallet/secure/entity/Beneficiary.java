package com.wallet.secure.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

/**
 * Entidad que representa un beneficiario de un seguro.
 * <p>
 * Contiene los datos personales y la relación con el titular del seguro.
 * </p>
 */
@Entity
@Table(name = "beneficiaries")
public class Beneficiary {

    /**
     * Identificador único del beneficiario.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private String relation; // HIJO, CONYUGE, ETC
    private String dni;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insurance_id")
    @JsonIgnore
    private Insurance insurance;

    public Beneficiary() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRelation() { return relation; }
    public void setRelation(String relation) { this.relation = relation; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public Insurance getInsurance() { return insurance; }
    public void setInsurance(Insurance insurance) { this.insurance = insurance; }
}
