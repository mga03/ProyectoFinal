package com.wallet.secure.config;

import com.wallet.secure.security.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Clase de configuración principal para la seguridad de Spring.
 * <p>
 * Define la cadena de filtros de seguridad, políticas CORS, gestión de sesiones sin estado (Stateless)
 * y la configuración del codificador de contraseñas.
 * </p>
 */
@Configuration
@EnableWebSecurity
@org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * Configura la cadena de filtros de seguridad HTTP.
     * <p>
     * - Deshabilita CSRF (API REST).<br>
     * - Configura CORS.<br>
     * - Establece sesión STATELESS.<br>
     * - Define reglas de autorización de endpoints.<br>
     * - Añade el filtro personalizado (TrustedHeaderFilter) para autenticación entre microservicios.
     * </p>
     *
     * @param http Objeto HttpSecurity para configurar la seguridad.
     * @return La cadena de filtros configurada.
     * @throws Exception Si hay error en la configuración.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/api/upload/**", "/uploads/**", "/api/users/approve-role", "/api/users/reject-role").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(new TrustedHeaderFilter(userDetailsService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configura la política de CORS (Cross-Origin Resource Sharing).
     * <p>Permite peticiones desde cualquier origen (para desarrollo) y métodos estándar.</p>
     *
     * @return Fuente de configuración de CORS.
     */
    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
        
        // --- CORRECCIÓN CLAVE: Permitir TODOS los orígenes para evitar errores de puertos ---
        configuration.setAllowedOriginPatterns(java.util.List.of("*")); 
        
        configuration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(java.util.List.of("*"));
        configuration.setAllowCredentials(true);
        
        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Define el bean para la encriptación de contraseñas usando BCrypt.
     *
     * @return Instancia de BCryptPasswordEncoder.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Filtro personalizado para confiar en el header de autenticación entre Gateway/Cliente y API.
     * <p>Permite la autenticación basada en el header 'X-Auth-User' sin necesidad de tokens JWT complejos en esta capa.</p>
     */
    public static class TrustedHeaderFilter extends OncePerRequestFilter {
        private final CustomUserDetailsService userDetailsService;

        public TrustedHeaderFilter(CustomUserDetailsService userDetailsService) {
            this.userDetailsService = userDetailsService;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {
            
            String userEmail = request.getHeader("X-Auth-User");
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                try {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } catch (Exception e) {
                    // Si el usuario no existe, sigue como anónimo
                }
            }
            filterChain.doFilter(request, response);
        }
    }
}