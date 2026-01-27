package com.wallet.secure.config;

import com.wallet.secure.dto.User;
import com.wallet.secure.service.ApiClientService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Collections;

/**
 * Interceptor que sincroniza el rol del usuario en la sesión con el de la base de datos (API)
 * en cada petición. Esto asegura que si un usuario es promovido a ADMIN, ve el panel inmediatamente.
 */
@Component
public class RoleSyncInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RoleSyncInterceptor.class);
    private final ApiClientService apiClientService;

    public RoleSyncInterceptor(ApiClientService apiClientService) {
        this.apiClientService = apiClientService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            try {
                // Obtener usuario fresco de la API
                String email = auth.getName();
                User freshUser = apiClientService.getUserByEmail(email);

                if (freshUser != null) {
                    // Normalizar roles
                    String currentRole = auth.getAuthorities().iterator().next().getAuthority();
                    String freshRole = freshUser.getRole();
                    if (!freshRole.startsWith("ROLE_")) freshRole = "ROLE_" + freshRole;

                    // Si hay discrepancia, actualizar la sesión
                    if (!currentRole.equals(freshRole)) {
                        logger.info("Sincronizando rol de usuario: {} de {} a {}", email, currentRole, freshRole);

                        // Si el principal es un UserDetails estándar de Spring Security
                        if (auth.getPrincipal() instanceof UserDetails existingDetails) {
                            UserDetails newDetails = new org.springframework.security.core.userdetails.User(
                                    existingDetails.getUsername(),
                                    existingDetails.getPassword(),
                                    existingDetails.isEnabled(),
                                    existingDetails.isAccountNonExpired(),
                                    existingDetails.isCredentialsNonExpired(),
                                    existingDetails.isAccountNonLocked(),
                                    Collections.singletonList(new SimpleGrantedAuthority(freshRole))
                            );

                            UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
                                    newDetails,
                                    auth.getCredentials(),
                                    newDetails.getAuthorities()
                            );
                            
                            SecurityContextHolder.getContext().setAuthentication(newAuth);
                        }
                    }
                }
            } catch (Exception e) {
                // Fallo silencioso para no bloquear la navegación si la API cae momentáneamente
                logger.warn("No se pudo sincronizar el rol del usuario: {}", e.getMessage());
            }
        }
        return true;
    }
}
