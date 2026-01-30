package com.wallet.secure.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa a los usuarios del sistema.
 * <p>
 * Gestiona la información de autenticación, roles, datos personales
 * y relaciones con otras entidades como seguros y tickets de soporte.
 * </p>
 */
@Entity
@Table(name = "users")
public class User {

    /**
     * Identificador único del usuario.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @jakarta.persistence.Column(length = 50)
    @jakarta.validation.constraints.Size(max = 50, message = "El nombre no puede superar los 50 caracteres")
    private String name;
    private String email;
    private String password;

    private String role;

    private boolean enabled = false;

    @Column(name = "verification_code", length = 64)
    private String verificationCode;

    @Column(name = "reset_token")
    private String resetToken;

    @Pattern(regexp = "[679][0-9]{8}", message = "Móvil inválido")
    private String mobile;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Insurance> insurances = new ArrayList<>();

    @com.fasterxml.jackson.annotation.JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ticket> tickets = new ArrayList<>();

    // Token de Aprobación de Rol (Evita necesidad de login del Admin)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private String roleChangeToken;
    
    @com.fasterxml.jackson.annotation.JsonIgnore
    private String requestedRole;

    // Constructors
    public User() {}

    public User(String name, String email, String password, String role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }

    public void setRole(String role) { this.role = role; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getVerificationCode() { return verificationCode; }
    public void setVerificationCode(String verificationCode) { this.verificationCode = verificationCode; }

    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public List<Insurance> getInsurances() { return insurances; }
    public void setInsurances(List<Insurance> insurances) { 
        this.insurances = insurances; 
    }

    public List<Ticket> getTickets() { return tickets; }
    public void setTickets(List<Ticket> tickets) { 
        this.tickets = tickets; 
    }

    public String getRoleChangeToken() { return roleChangeToken; }
    public void setRoleChangeToken(String roleChangeToken) { this.roleChangeToken = roleChangeToken; }

    public String getRequestedRole() { return requestedRole; }
    public void setRequestedRole(String requestedRole) { this.requestedRole = requestedRole; }
    
    // Helper method to add insurance
    public void addInsurance(Insurance insurance) {
        insurances.add(insurance);
        insurance.setUser(this);
    }
    @Column(name = "avatar_url")
    private String avatarUrl;

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
