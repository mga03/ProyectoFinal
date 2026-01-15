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
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Public routes (authentication, registration, verification, password recovery, role approval)
                .requestMatchers("/login", "/register/**", "/verify/**", "/forgot-password/**", "/reset-password/**", "/role-approval/**", "/css/**", "/js/**", "/api/**").permitAll()
                
                // Admin Area (Full Access)
                .requestMatchers("/admin/**", "/delete/**", "/edit/**", "/new", "/save", "/support/**").hasRole("ADMIN")

                // All others (Dashboard, Details, Claims, etc) -> Authenticated (Consumers/Workers)
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
