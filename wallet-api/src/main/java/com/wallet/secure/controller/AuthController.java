package com.wallet.secure.controller;

import com.wallet.secure.entity.User;
import com.wallet.secure.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador REST para gestionar la autenticación y el registro de usuarios.
 * Provee endpoints para login, registro, verificación de cuenta y recuperación de contraseña.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final UserService userService;
    private final PasswordEncoder passwordEncoder; 

    public AuthController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Autentica a un usuario verificando sus credenciales.
     *
     * @param credentials Mapa que contiene el email y la contraseña.
     * @return ResponseEntity con el usuario autenticado o un mensaje de error.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        System.out.println("INFO: Intento de inicio de sesión: " + email);
        
        User user = userService.findUserByEmail(email);
        
        if (user == null) {
             System.out.println("WARN: Usuario no encontrado: " + email);
             return ResponseEntity.status(401).body(Map.of("message", "User not found")); 
        }
        
        System.out.println("INFO: Usuario encontrado. Rol: " + user.getRole() + ", Activo: " + user.isEnabled());

        if (!passwordEncoder.matches(password, user.getPassword())) {
             System.out.println("WARN: Contraseña incorrecta para el usuario: " + email);
             return ResponseEntity.status(401).body(Map.of("message", "Invalid credentials"));
        }
        
        if (!user.isEnabled()) {
             System.out.println("WARN: Cuenta no verificada: " + email);
             return ResponseEntity.status(401).body(Map.of("message", "Account not verified"));
        }

        System.out.println("INFO: Inicio de sesión exitoso: " + email);
        return ResponseEntity.ok(user);
    }

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param user Objeto Usuario con los datos de registro.
     * @return ResponseEntity indicando el resultado de la operación.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody User user) {
        try {
            userService.registerUser(user);
            return ResponseEntity.ok(Map.of("message", "User registered successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    
    /**
     * Verifica la cuenta de un usuario mediante un código.
     *
     * @param code Código de verificación enviado por correo.
     * @return ResponseEntity con el resultado de la verificación.
     */
    @GetMapping("/verify")
    public ResponseEntity<?> verifyAccount(@RequestParam("code") String code) {
        boolean verified = userService.verifyUser(code);
        if (verified) {
             return ResponseEntity.ok(Map.of("message", "Account verified"));
        } else {
             return ResponseEntity.badRequest().body(Map.of("message", "Invalid verification code"));
        }
    }
    
    /**
     * Inicia el proceso de recuperación de contraseña enviando un correo al usuario.
     *
     * @param email Email del usuario que desea recuperar la contraseña.
     * @return ResponseEntity indicando si el correo de recuperación fue enviado.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam("email") String email) {
        try {
            userService.initiatePasswordRecovery(email);
            return ResponseEntity.ok(Map.of("message", "Recovery email sent"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Establece una nueva contraseña utilizando un token de recuperación.
     *
     * @param payload Mapa con el token y la nueva contraseña.
     * @return ResponseEntity con el resultado del cambio de contraseña.
     */
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
