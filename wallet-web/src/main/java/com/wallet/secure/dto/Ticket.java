package com.wallet.secure.dto;

import java.time.LocalDateTime;

/**
 * DTO que representa un ticket de soporte.
 * Contiene el asunto, mensaje, estado y la asociación con el usuario creador.
 */
public class Ticket {

    private Long id;

    private String subject;
    
    private String message;
    
    private Status status;

    private LocalDateTime createdAt;
    
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
