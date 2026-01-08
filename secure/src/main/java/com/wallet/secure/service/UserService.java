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

    // Quitamos @Transactional global para manejar nosotros la transacción del email
    public void registerUser(User user, String role, String roleToken) throws Exception {
        
        // 1. Validar si existe
        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new Exception("El email ya está registrado.");
        }

        // 2. Validar Token de Rol
        if (!isValidRoleToken(role, roleToken)) {
            throw new Exception("Clave de rol incorrecta.");
        }

        // 3. Preparar Usuario
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(role);
        user.setEnabled(false); // Requiere verificación
        user.setVerificationCode(UUID.randomUUID().toString());

        // 4. GUARDAR USUARIO (Esto asegura que el usuario se crea en BD)
        User savedUser = userRepository.save(user);

        // 5. INTENTAR ENVIAR EMAIL (A prueba de fallos)
        try {
            emailService.sendVerificationEmail(savedUser.getEmail(), savedUser.getVerificationCode());
        } catch (Exception e) {
            // SI FALLA EL EMAIL (Por configuración SMTP), NO BORRAMOS EL USUARIO.
            // Imprimimos el link en consola para desarrollo.
            System.err.println("=================================================");
            System.err.println("⚠️ ERROR SMTP: No se pudo enviar el correo a " + savedUser.getEmail());
            System.err.println("🔗 LINK DE ACTIVACIÓN (COPIA Y PEGA EN NAVEGADOR):");
            System.err.println("http://localhost:8081/verify?code=" + savedUser.getVerificationCode());
            System.err.println("=================================================");
            // No lanzamos excepción para permitir que el registro termine con éxito
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

    // Métodos auxiliares (Recuperación contraseña, etc.) - Mantenlos como los tenías o cópialos del chat anterior
    @Transactional(rollbackFor = Exception.class)
    public void initiatePasswordRecovery(String email) throws Exception {
         // ... (código existente)
    }

    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(String token, String newPassword) throws Exception {
        // ... (código existente)
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