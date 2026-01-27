package com.wallet.secure.security;

import com.wallet.secure.dto.User;
import com.wallet.secure.service.ApiClientService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Servicio de seguridad personalizado para la aplicación web.
 * Consume la API para validar la autenticación del usuario.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final ApiClientService apiClientService;

    public CustomUserDetailsService(ApiClientService apiClientService) {
        this.apiClientService = apiClientService;
    }

    /**
     * Carga el usuario llamando a la API con su correo electrónico.
     *
     * @param email Correo electrónico del usuario.
     * @return UserDetails con la información obtenida de la API.
     * @throws UsernameNotFoundException Si la API no retorna el usuario.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = apiClientService.getUserByEmail(email); // Requires API to expose this
        if (user == null) {
            System.err.println("LOGIN ERROR: Usuario no encontrado via API: " + email);
            throw new UsernameNotFoundException("User not found via API: " + email);
        }

        System.out.println("INFO: Intento de inicio de sesión: " + email + " | Rol: " + user.getRole());
        
        String roleName = user.getRole();
        if (roleName != null && !roleName.startsWith("ROLE_")) {
             roleName = "ROLE_" + roleName;
        }

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(), // Hashed password from API
                user.isEnabled(),
                true,
                true,
                true,
                Collections.singletonList(new SimpleGrantedAuthority(roleName != null ? roleName : "ROLE_USER"))
        );
    }
}
