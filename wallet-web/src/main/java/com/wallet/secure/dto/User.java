package com.wallet.secure.dto;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

// IMPLEMENTAMOS UserDetails PARA QUE SPRING LO ENTIENDA
public class User implements UserDetails {
    
    private Long id;
    private String name;
    private String email;
    private String password;
    private String mobile;
    private String role;
    private boolean enabled;
    
    // --- Getters y Setters normales ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    // --- MÉTODOS OBLIGATORIOS DE SPRING SECURITY (UserDetails) ---
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role == null) return Collections.emptyList();
        // Aseguramos que el rol tenga el prefijo ROLE_
        String roleName = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return Collections.singletonList(new SimpleGrantedAuthority(roleName));
    }

    @Override
    public String getPassword() { return password; }

    @Override // ¡IMPORTANTE! El username para Spring será el EMAIL
    public String getUsername() { return email; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return enabled; }
}