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
    // Método simplificado: Solo recibe el objeto User
    public void registerUser(User user) throws Exception {

        // 1. Validar si el email ya existe
        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new Exception("El email ya está registrado.");
        }

        // 2. ASIGNACIÓN AUTOMÁTICA DE ROL
        // Por seguridad, todos nacen como trabajadores.
        // Si se requiere un Admin, se cambia manualmente en BD.
        user.setRole("ROLE_WORKER");

        // 3. Preparar Usuario (Encriptar pass, generar código, deshabilitar)
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(false); // Requiere verificación por email
        user.setVerificationCode(UUID.randomUUID().toString());

        // 4. Guardar Usuario
        User savedUser = userRepository.save(user);

        // 5. Enviar Email (Fail-Safe: si falla, muestra link en consola)
        try {
            emailService.sendVerificationEmail(savedUser.getEmail(), savedUser.getVerificationCode());
        } catch (Exception e) {
            System.err.println("⚠️ SMTP ERROR: No se envió el correo.");
            System.err.println("🔗 LINK ACTIVACIÓN (DEV): http://localhost:8081/verify?code=" + savedUser.getVerificationCode());
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
    
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email);
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
                System.err.println("ERROR CORREO RECUPERACIÓN: " + e.getMessage());
                System.err.println("LINK RECUPERACIÓN MANUAL (DEV):");
                System.err.println("http://localhost:8081/reset-password?token=" + token);
            }
        }
        // Nota: Si el usuario no existe, no hacemos nada por seguridad (para no revelar correos)
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateRole(String email, String newRole) throws Exception {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            user.setRole(newRole);
            userRepository.saveAndFlush(user); // Force commit
        } else {
            throw new Exception("Usuario no encontrado con email: " + email);
        }
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




    // --- NUEVO: Gestión de roles con token (Stateless) ---
    @Transactional
    public void requestRoleChange(String email, String desiredRole) throws Exception {
        User user = userRepository.findByEmail(email);
        if (user == null) throw new Exception("Usuario no encontrado.");

        String token = UUID.randomUUID().toString();
        user.setRoleChangeToken(token);
        user.setRequestedRole(desiredRole);
        userRepository.save(user);

        // Envía el correo con el token
        emailService.sendAdminRoleRequest(user.getEmail(), desiredRole, token);
    }

    @Transactional
    public void approveRoleChange(String token) throws Exception {
        // Buscamos usuario por el token (necesitamos añadir el método al repositorio o buscar manualmente)
        // Como no tenemos el método en repo aún, recuperamos por token lo añadiremos.
        // HACK TEMPORAL: Buscar entre todos (ineficiente pero funciona para pocos users) o añadir metodo FindBy.
        // Lo ideal es añadir findByRoleChangeToken en UserRepository.
        
        // Asumiendo que añadiremos el metodo al repo en el siguiente paso.
        User user = userRepository.findByRoleChangeToken(token);
        
        if (user == null) {
            throw new Exception("Token de solicitud inválido o no encontrado.");
        }

        if (user.getRequestedRole() == null) {
             throw new Exception("No hay rol pendiente de aprobación.");
        }

        user.setRole(user.getRequestedRole());
        user.setRoleChangeToken(null);
        user.setRequestedRole(null);
        
        userRepository.save(user);
        
        emailService.sendRoleStatusEmail(user.getEmail(), "APROBADO", user.getRole());
    }

    @Transactional
    public void rejectRoleChange(String token) throws Exception {
        User user = userRepository.findByRoleChangeToken(token);
        
        if (user == null) {
            throw new Exception("Token de solicitud inválido o no encontrado.");
        }

        // Limpiamos los campos de solicitud sin cambiar el rol
        user.setRoleChangeToken(null);
        user.setRequestedRole(null);
        
        userRepository.save(user);
        
        // Notificamos al usuario
        emailService.sendRoleStatusEmail(user.getEmail(), "DENEGADO", user.getRole());
    }

}