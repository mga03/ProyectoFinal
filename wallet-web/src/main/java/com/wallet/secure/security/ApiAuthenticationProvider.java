package com.wallet.secure.security;

import com.wallet.secure.dto.User;
import com.wallet.secure.service.ApiClientService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * Proveedor de autenticación personalizado que delega la verificación al backend (API).
 * Realiza una llamada al servicio {@link ApiClientService} para validar las credenciales.
 */
@Component
public class ApiAuthenticationProvider implements AuthenticationProvider {

    private final ApiClientService apiClientService;

    public ApiAuthenticationProvider(ApiClientService apiClientService) {
        this.apiClientService = apiClientService;
    }

    /**
     * Realiza la autenticación del usuario llamando a la API.
     *
     * @param authentication Objeto de autenticación con el correo y contraseña.
     * @return Objeto Authentication completamente poblado (incluyendo roles) si es exitoso.
     * @throws AuthenticationException Si las credenciales son inválidas o la cuenta no está verificada.
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String password = authentication.getCredentials().toString();

        // 1. Llamamos a la API para verificar credenciales
        User user = apiClientService.verifyLogin(email, password);

        if (user == null) {
            throw new BadCredentialsException("Email o contraseña incorrectos, o cuenta no verificada.");
        }

        // 2. CORRECCIÓN DE ROLES: Asegurar prefijo ROLE_
        String role = user.getRole();
        if (role != null && !role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }

        // 3. Crear sesión válida en la Web
        return new UsernamePasswordAuthenticationToken(
                user, // Principal (UserDetails o User DTO)
                password, // Credentials
                Collections.singleton(new SimpleGrantedAuthority(role)) // Authority corregida
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}