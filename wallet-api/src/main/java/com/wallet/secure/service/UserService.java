package com.wallet.secure.service;

import com.wallet.secure.entity.User;
import com.wallet.secure.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Servicio encargado de la lógica de negocio relacionada con los usuarios.
 * Gestiona el registro, verificación, recuperación de contraseñas y cambios de roles.
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    /**
     * Registra un nuevo usuario en el sistema.
     * <p>
     * Se encarga de validar si el email existe, asignar el rol por defecto (ROLE_WORKER),
     * encriptar la contraseña, generar el código de verificación y guardar el usuario en estado inactivo.
     * Finalmente, intenta enviar el correo de verificación.
     * </p>
     *
     * @param user El objeto usuario con los datos del registro.
     * @throws Exception Si el email ya está registrado.
     */
    public void registerUser(User user) throws Exception {

        // 1. Validar si el email ya existe
        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new Exception("El email ya está registrado.");
        }

        // 2. Asignación automática de rol
        // Por seguridad, todos comienzan con el rol de trabajador (ROLE_WORKER).
        // Si se requiere un Administrador, se debe cambiar manualmente en la base de datos o mediante proceso de aprobación.
        user.setRole("ROLE_WORKER");

        // 3. Preparar Usuario (Encriptar contraseña, generar código de verificación, establecer como inactivo)
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(false); // Requiere verificación por email para activarse
        user.setVerificationCode(UUID.randomUUID().toString());

        // 4. Guardar Usuario
        User savedUser = userRepository.save(user);

        // 5. Enviar Email (Mecanismo de seguridad: si falla, muestra el enlace en la consola)
        try {
            emailService.sendVerificationEmail(savedUser.getEmail(), savedUser.getVerificationCode());
        } catch (Exception e) {
            System.err.println("SMTP ERROR: No se envió el correo.");
            System.err.println("ENLACE DE ACTIVACIÓN (DEV): http://localhost:8081/verify?code=" + savedUser.getVerificationCode());
        }
    }

    /**
     * Verifica la cuenta de usuario mediante el código enviado por correo.
     *
     * @param code El código de verificación único.
     * @return true si la verificación fue exitosa, false si el código es inválido o el usuario ya está activo.
     */
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
    
    /**
     * Busca un usuario por su dirección de correo electrónico.
     *
     * @param email El correo electrónico del usuario.
     * @return El objeto User si existe, de lo contrario null.
     */
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Inicia el proceso de recuperación de contraseña.
     * <p>
     * Genera un token de reseteo y lo envía al correo del usuario.
     * Si el envío falla, imprime el enlace de recuperación en la consola para desarrollo.
     * </p>
     *
     * @param email El correo electrónico del usuario que solicita la recuperación.
     * @throws Exception Si ocurre un error durante el proceso.
     */
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
                System.err.println("ENLACE DE RECUPERACIÓN MANUAL (DEV):");
                System.err.println("http://localhost:8081/reset-password?token=" + token);
            }
        }
        // Nota: Si el usuario no existe, no se realiza ninguna acción por seguridad.
    }

    /**
     * Actualiza el rol de un usuario específico.
     *
     * @param email El correo del usuario a modificar.
     * @param newRole El nuevo rol a asignar.
     * @throws Exception Si el usuario no es encontrado.
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(String email, String newRole) throws Exception {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            user.setRole(newRole);
            userRepository.saveAndFlush(user); // Forzar la confirmación de la transacción
        } else {
            throw new Exception("Usuario no encontrado con email: " + email);
        }
    }

    /**
     * Establece una nueva contraseña utilizando un token de recuperación válido.
     *
     * @param token El token de recuperación.
     * @param newPassword La nueva contraseña a establecer.
     * @throws Exception Si el token es inválido, expirado o hay error al guardar.
     */
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(String token, String newPassword) throws Exception {
        User user = userRepository.findByResetToken(token);
        if (user == null) {
            throw new Exception("Token inválido o expirado.");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        
        // Usar saveAndFlush y capturar excepciones de validación
        try {
            userRepository.saveAndFlush(user);
        } catch (Exception e) {
            throw new Exception("Error guardando usuario (posible dato inválido): " + e.getMessage());
        }
    }

    // --- Gestión de roles con token (Stateless) ---

    /**
     * Solicita un cambio de rol para el usuario.
     * <p>
     * Genera un token de solicitud y envía una notificación al administrador para su aprobación.
     * </p>
     *
     * @param email El correo del usuario solicitante.
     * @param desiredRole El rol que el usuario desea obtener.
     * @throws Exception Si el usuario no existe.
     */
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

    /**
     * Aprueba la solicitud de cambio de rol basada en el token proporcionado.
     *
     * @param token El token de solicitud de cambio de rol.
     * @throws Exception Si el token es inválido o no se encuentra la solicitud.
     */
    @Transactional
    public void approveRoleChange(String token) throws Exception {
        System.out.println("INFO: Intentando aprobar rol con token: " + token);
        
        User user = userRepository.findByRoleChangeToken(token);
        
        if (user == null) {
            System.out.println("INFO: Usuario no encontrado para el token: " + token);
            // Intento de depuración adicional: imprimir todos los tokens (solo para desarrollo)
            userRepository.findAll().forEach(u -> 
                System.out.println("INFO: User " + u.getEmail() + " Token: " + u.getRoleChangeToken())
            );
            throw new Exception("Token de solicitud inválido o no encontrado.");
        }

        System.out.println("INFO: Usuario encontrado: " + user.getEmail() + ", Rol solicitado: " + user.getRequestedRole());

        if (user.getRequestedRole() == null) {
            System.out.println("INFO: No hay rol solicitado.");
             throw new Exception("No hay rol pendiente de aprobación.");
        }

        user.setRole(user.getRequestedRole());
        user.setRoleChangeToken(null);
        user.setRequestedRole(null);
        
        userRepository.save(user);
        
        System.out.println("INFO: Rol actualizado correctamente a " + user.getRole());
        
        try {
            emailService.sendRoleStatusEmail(user.getEmail(), "APROBADO", user.getRole());
        } catch (Exception e) {
             System.out.println("INFO: Error enviando email de confirmación: " + e.getMessage());
        }
    }

    /**
     * Rechaza la solicitud de cambio de rol.
     *
     * @param token El token de la solicitud a rechazar.
     * @throws Exception Si el token es inválido.
     */
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