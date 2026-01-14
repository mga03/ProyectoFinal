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
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Public routes (authentication, registration, verification, password recovery)
                .requestMatchers("/login", "/register/**", "/verify/**", "/forgot-password/**", "/reset-password/**", "/css/**", "/js/**", "/api/**").permitAll()
                
                // Admin Area
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // Insurance Delete (Admin & Manager)
                .requestMatchers("/insurance/delete/**").hasAnyRole("ADMIN", "MANAGER")

                // Insurance Edit/Create (Admin & Manager)
                .requestMatchers("/insurance/edit/**", "/new", "/save").hasAnyRole("ADMIN", "MANAGER")

                // Insurance Claims (Admin, Manager, Worker)
                .requestMatchers("/insurance/{id}/claims/**").hasAnyRole("ADMIN", "MANAGER", "WORKER")

                // Support (Admin & Collaborator)
                .requestMatchers("/support/**").hasAnyRole("ADMIN", "COLLABORATOR") // Example rule from request

                // All others (Dashboard, Details, etc) -> Authenticated
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
        
        // Ensure H2 console or API works if needed, but for now standard config
        http.csrf(csrf -> csrf.ignoringRequestMatchers("/api/**")); // Disable CSRF for API simplicity

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
