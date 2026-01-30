package com.wallet.secure.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa un ticket de soporte técnico o atención al cliente.
 * <p>
 * Permite a los usuarios comunicar incidencias, las cuales son gestionadas
 * por los administradores o personal de soporte.
 * </p>
 */
@Entity
@Table(name = "tickets")
public class Ticket {

    /**
     * Identificador único del ticket.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String subject;
    
    @Column(columnDefinition = "TEXT")
    private String message;
    
    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public enum Status {
        OPEN, CLOSED
    }

    public Ticket() {
        this.createdAt = LocalDateTime.now();
        this.status = Status.OPEN;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
