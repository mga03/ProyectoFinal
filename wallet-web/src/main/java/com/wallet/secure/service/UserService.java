package com.wallet.secure.service;

import com.wallet.secure.dto.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Servicio intermediario para la gestión de usuarios en el frontend.
 * <p>
 * Delega las operaciones de negocio al {@link ApiClientService} para comunicarse con el backend.
 * </p>
 */
@Service
public class UserService {

    @Autowired
    private ApiClientService apiClientService;

    /**
     * Registra un nuevo usuario delegando la llamada a la API.
     *
     * @param user Objeto User con los datos de registro.
     * @throws Exception Si ocurre un error en el registro.
     */
    public void registerUser(User user) throws Exception {
        // Delegate to API
        // UserDTO is passed, password should be plaintext, API encrypts it.
        apiClientService.registerUser(user);
    }

    /**
     * Verifica la cuenta de usuario mediante el código de activación.
     *
     * @param code Código de verificación.
     * @return true si la verificación fue exitosa.
     */
    public boolean verifyUser(String code) {
        return apiClientService.verifyUser(code);
    }
    
    /**
     * Busca un usuario por su email.
     *
     * @param email Correo electrónico.
     * @return El usuario encontrado o null.
     */
    public User findUserByEmail(String email) {
        return apiClientService.getUserByEmail(email);
    }

    /**
     * Inicia el proceso de recuperación de contraseña.
     *
     * @param email Correo del usuario.
     * @throws Exception Si falla la comunicación con la API.
     */
    public void initiatePasswordRecovery(String email) throws Exception {
        apiClientService.initiatePasswordRecovery(email);
    }

    /**
     * Restablece la contraseña del usuario.
     *
     * @param token Token de seguridad.
     * @param newPassword Nueva contraseña.
     * @throws Exception Si falla la operación.
     */
    public void resetPassword(String token, String newPassword) throws Exception {
        apiClientService.resetPassword(token, newPassword);
    }

    /**
     * Solicita un cambio de rol (Placeholder/Delegación futura).
     *
     * @param email Email del usuario.
     * @param desiredRole Rol deseado.
     * @throws Exception Si ocurre un error.
     */
    public void requestRoleChange(String email, String desiredRole) throws Exception {
        // API task?
    }

    /**
     * Aprueba un cambio de rol (Placeholder/Delegación futura).
     *
     * @param token Token de aprobación.
     * @throws Exception Si ocurre un error.
     */
    public void approveRoleChange(String token) throws Exception {
        // API task?
    }

    /**
     * Rechaza un cambio de rol (Placeholder/Delegación futura).
     *
     * @param token Token de rechazo.
     * @throws Exception Si ocurre un error.
     */
    public void rejectRoleChange(String token) throws Exception {
        // API task?
    }
}