package com.wallet.secure.service;

import com.wallet.secure.entity.User;
import com.wallet.secure.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    // Eliminamos @Transactional a nivel de método para controlar nosotros el guardado parcial
    public void registerUser(User user, String role, String roleToken) throws Exception {
        
        // 1. Validaciones previas
        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new Exception("El email ya está registrado.");
        }
        
        // Validación manual del token de rol (case insensitive)
        String cleanToken = (roleToken != null) ? roleToken.trim().toLowerCase() : "";
        boolean tokenValido = false;
        switch (role) {
            case "ROLE_ADMIN": tokenValido = "administrador".equals(cleanToken); break;
            case "ROLE_MANAGER": tokenValido = "gestor".equals(cleanToken); break;
            case "ROLE_WORKER": tokenValido = "trabajador".equals(cleanToken); break;
            case "ROLE_COLLABORATOR": tokenValido = "colaborador".equals(cleanToken); break;
        }
        if (!tokenValido) {
            throw new Exception("La Clave de Rol es incorrecta.");
        }

        // 2. Preparar Usuario
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(role);
        user.setEnabled(false); // Inactivo por defecto
        user.setVerificationCode(UUID.randomUUID().toString());

        // 3. GUARDAR USUARIO (Commit inmediato a la DB)
        User savedUser = userRepository.save(user);

        // 4. INTENTAR ENVIAR EMAIL (Dentro de try-catch para no romper el flujo)
        try {
            emailService.sendVerificationEmail(savedUser.getEmail(), savedUser.getVerificationCode());
        } catch (Exception e) {
            // Si falla el SMTP, no borramos el usuario. Mostramos link de emergencia.
            System.err.println("⚠️ NO SE PUDO ENVIAR EL CORREO (Revisa application.properties)");
            System.err.println("🔗 LINK MANUAL (DEV): http://localhost:8081/verify?code=" + savedUser.getVerificationCode());
        }
    }

    public boolean verifyUser(String code) {
        User user = userRepository.findByVerificationCode(code);
        if (user == null || user.isEnabled()) {
            return false;
        }
        user.setVerificationCode(null);
        user.setEnabled(true);
        userRepository.save(user);
        return true;
    }

    // Eliminamos @Transactional global para este método para manejar el try-catch
    public void initiatePasswordRecovery(String email) throws Exception {
        User user = userRepository.findByEmail(email);
        
        if (user != null) {
            // 1. Generar y Guardar el Token
            String token = UUID.randomUUID().toString();
            user.setResetToken(token);
            userRepository.save(user); // Guardamos el token ANTES de enviar el correo
            
            // 2. Intentar enviar el correo
            try {
                emailService.sendPasswordResetEmail(user.getEmail(), token);
            } catch (Exception e) {
                // Si falla el correo, mostramos el link en consola para no bloquear al usuario
                System.err.println("❌ ERROR CORREO RECUPERACIÓN: " + e.getMessage());
                System.err.println("👉 LINK RECUPERACIÓN MANUAL (DEV):");
                System.err.println("http://localhost:8081/reset-password?token=" + token);
            }
        }
        // Nota: Si el usuario no existe, no hacemos nada por seguridad (para no revelar correos)
    }

    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(String token, String newPassword) throws Exception {
        User user = userRepository.findByResetToken(token);
        if (user == null) {
            throw new Exception("Token inválido o expirado.");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        
        // USAR saveAndFlush Y CAPTURAR EXCEPCIONES DE VALIDACIÓN
        try {
            userRepository.saveAndFlush(user);
        } catch (Exception e) {
            throw new Exception("Error guardando usuario (posible dato inválido): " + e.getMessage());
        }
    }

    private boolean isValidRoleToken(String role, String token) {
        if (token == null) return false;
        String lowerToken = token.trim().toLowerCase();
        switch (role) {
            case "ROLE_ADMIN": return "administrador".equals(lowerToken);
            case "ROLE_MANAGER": return "gestor".equals(lowerToken);
            case "ROLE_WORKER": return "trabajador".equals(lowerToken);
            case "ROLE_COLLABORATOR": return "colaborador".equals(lowerToken);
            default: return false;
        }
    }
}