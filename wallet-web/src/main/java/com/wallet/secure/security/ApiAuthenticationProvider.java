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

@Component
public class ApiAuthenticationProvider implements AuthenticationProvider {

    private final ApiClientService apiClientService;

    public ApiAuthenticationProvider(ApiClientService apiClientService) {
        this.apiClientService = apiClientService;
    }

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