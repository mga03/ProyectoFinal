package com.wallet.secure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, com.wallet.secure.security.ApiAuthenticationProvider authProvider) throws Exception {
        http
            .authenticationProvider(authProvider)
            .authorizeHttpRequests(auth -> auth
                // Rutas Públicas y Recursos Estáticos (IMPRESCINDIBLE permitir uploads e images)
                .requestMatchers("/login", "/register/**", "/verify/**", "/forgot-password/**", "/reset-password/**", "/role-approval/**", "/css/**", "/js/**", "/images/**", "/uploads/**", "/webjars/**").permitAll()
                
                // 1. ZONA ADMIN
                .requestMatchers("/admin/**").hasAnyRole("ADMIN")

                // 2. ZONA OPERATIVA
                .requestMatchers("/new", "/save", "/edit/**", "/delete/**", "/support/**", "/insurances/**", "/profile/**")
                    .hasAnyRole("ADMIN", "MANAGER", "WORKER")

                // Resto requiere login
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true) // Forzar ir al inicio tras login
                .usernameParameter("email")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            // ⚠️ CRÍTICO: Desactivar CSRF temporalmente para que funcionen los formularios POST entre servidores
            .csrf(csrf -> csrf.disable());

        return http.build();
    }
}