package com.wallet.secure.controller;

import com.wallet.secure.entity.User;
import com.wallet.secure.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final UserService userService;
    private final PasswordEncoder passwordEncoder; // To verify password

    public AuthController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        System.out.println("🔍 LOGIN ATTEMPT: " + email);
        
        User user = userService.findUserByEmail(email);
        
        if (user == null) {
             System.out.println("❌ User not found");
             return ResponseEntity.status(401).body(Map.of("message", "User not found")); 
        }
        
        System.out.println("👤 User Found. Role: " + user.getRole() + ", Enabled: " + user.isEnabled());
        // System.out.println("🔑 Hash in DB: " + user.getPassword()); // SOLO PARA DEBUG LOCAL

        if (!passwordEncoder.matches(password, user.getPassword())) {
             System.out.println("❌ Password mismatch! Input len: " + (password != null ? password.length() : "null"));
             return ResponseEntity.status(401).body(Map.of("message", "Invalid credentials"));
        }
        
        if (!user.isEnabled()) {
             System.out.println("❌ User not enabled");
             return ResponseEntity.status(401).body(Map.of("message", "Account not verified"));
        }

        System.out.println("✅ LOGIN SUCCESS");
        return ResponseEntity.ok(user);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody User user) {
        try {
            userService.registerUser(user);
            return ResponseEntity.ok(Map.of("message", "User registered successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    
    @GetMapping("/verify")
    public ResponseEntity<?> verifyAccount(@RequestParam("code") String code) {
        boolean verified = userService.verifyUser(code);
        if (verified) {
             return ResponseEntity.ok(Map.of("message", "Account verified"));
        } else {
             return ResponseEntity.badRequest().body(Map.of("message", "Invalid verification code"));
        }
    }
    
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam("email") String email) {
        try {
            userService.initiatePasswordRecovery(email);
            return ResponseEntity.ok(Map.of("message", "Recovery email sent"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> payload) {
        try {
            String token = payload.get("token");
            String password = payload.get("password");
            userService.resetPassword(token, password);
            return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
