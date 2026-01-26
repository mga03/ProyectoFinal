package com.wallet.secure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
                // Rutas Públicas (Login, Registro, Estáticos...)
                .requestMatchers("/login", "/register/**", "/verify/**", "/forgot-password/**", "/reset-password/**", "/role-approval/**", "/css/**", "/js/**", "/api/**").permitAll()
                
                // 1. ZONA ADMIN (Exclusiva para gestión de usuarios y tickets globales)
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // 2. ZONA OPERATIVA (Crear Seguros, Editar, Borrar, Soporte)
                // Aquí es donde damos permiso a los TRABAJADORES (WORKER)
                .requestMatchers("/new", "/save", "/edit/**", "/delete/**", "/support/**", "/insurances/**")
                    .hasAnyRole("ADMIN", "MANAGER", "WORKER")

                // Resto de peticiones autenticadas
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/")
                .usernameParameter("email")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );
        
        http.csrf(csrf -> csrf.ignoringRequestMatchers("/api/**")); 

        return http.build();
    }
}
