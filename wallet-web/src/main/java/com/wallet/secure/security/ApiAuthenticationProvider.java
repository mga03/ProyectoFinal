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

        User user = apiClientService.verifyLogin(email, password);

        if (user == null) {
            throw new BadCredentialsException("Email o contraseña incorrectos, o cuenta no verificada.");
        }

        return new UsernamePasswordAuthenticationToken(
                user, 
                password, 
                Collections.singleton(new SimpleGrantedAuthority(user.getRole()))
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
