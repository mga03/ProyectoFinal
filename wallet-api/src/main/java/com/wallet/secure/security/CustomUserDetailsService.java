package com.wallet.secure.security;

import com.wallet.secure.entity.User;
import com.wallet.secure.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Servicio de seguridad personalizado que carga los detalles del usuario desde la base de datos
 * para la autenticación de Spring Security.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Carga un usuario por su nombre de usuario (en este caso, el correo electrónico).
     *
     * @param email El correo electrónico del usuario.
     * @return UserDetails con la información del usuario y sus roles.
     * @throws UsernameNotFoundException Si el usuario no es encontrado.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            System.err.println("LOGIN ERROR: Usuario no encontrado: " + email);
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        System.out.println("INFO: Intento de inicio de sesión: " + email + " | Rol en BD: " + user.getRole());
        
        // Asegurar que el rol tenga el prefijo ROLE_ si falta (verificación de seguridad)
        String roleName = user.getRole();
        if (!roleName.startsWith("ROLE_")) {
             System.out.println("WARN: Al rol " + roleName + " le falta el prefijo. Agregando ROLE_ temporalmente.");
             roleName = "ROLE_" + roleName;
        }

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.isEnabled(),
                true,
                true,
                true,
                Collections.singletonList(new SimpleGrantedAuthority(roleName))
        );
    }
}
