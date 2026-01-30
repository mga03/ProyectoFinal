package com.wallet.secure.repository;

import com.wallet.secure.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la gestión de datos de usuarios.
 * <p>
 * Proporciona métodos CRUD y consultas personalizadas para buscar usuarios
 * por email, token de verificación, token de reinicio de contraseña y rol.
 * </p>
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Busca un usuario por su correo electrónico.
     *
     * @param email El correo electrónico del usuario.
     * @return El usuario encontrado o null si no existe.
     */
    User findByEmail(String email);
    
    /**
     * Busca un usuario por su código de verificación.
     *
     * @param verificationCode El código de verificación enviado por email.
     * @return El usuario asociado al código o null.
     */
    User findByVerificationCode(String verificationCode);
    
    /**
     * Busca un usuario por su token de restablecimiento de contraseña.
     *
     * @param resetToken El token de restablecimiento.
     * @return El usuario asociado al token o null.
     */
    User findByResetToken(String resetToken);
    
    /**
     * Cuenta el número de usuarios que tienen un rol específico.
     *
     * @param role El rol a contar (ej: "ROLE_ADMIN").
     * @return El número de usuarios con ese rol.
     */
    long countByRole(String role);

    // Nuevo para cambio de rol stateless
    /**
     * Busca un usuario por el token de cambio de rol.
     *
     * @param roleChangeToken El token para aprobar el cambio de rol.
     * @return El usuario asociado al token o null.
     */
    User findByRoleChangeToken(String roleChangeToken);
}
