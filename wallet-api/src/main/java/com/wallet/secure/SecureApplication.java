package com.wallet.secure;

import com.wallet.secure.entity.User;
import com.wallet.secure.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class SecureApplication {

    private static final Logger logger = LoggerFactory.getLogger(SecureApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SecureApplication.class, args);
        logger.info("Aplicacion Wallet Secure iniciada correctamente.");
    }

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // 1. SUPER ADMIN (El dueño del sistema)
            User superAdmin = userRepository.findByEmail("guarinosmanuel07@gmail.com");
            
            if (superAdmin == null) {
                // SOLO si no existe, lo creamos con la contraseña por defecto
                superAdmin = new User();
                superAdmin.setName("Manuel Guarinos (Super Admin)");
                superAdmin.setEmail("guarinosmanuel07@gmail.com");
                superAdmin.setPassword(passwordEncoder.encode("1234")); // Clave inicial
                superAdmin.setRole("ROLE_ADMIN");
                superAdmin.setEnabled(true);
                userRepository.save(superAdmin);
                logger.info("✅ SUPER ADMIN creado: guarinosmanuel07@gmail.com / 1234");
            } else {
                // Si YA existe, solo nos aseguramos de que siga siendo ADMIN, pero NO tocamos la contraseña
                boolean changed = false;
                if (!"ROLE_ADMIN".equals(superAdmin.getRole())) {
                    superAdmin.setRole("ROLE_ADMIN");
                    changed = true;
                }
                if (!superAdmin.isEnabled()) {
                    superAdmin.setEnabled(true);
                    changed = true;
                }
                
                if (changed) {
                    userRepository.save(superAdmin);
                    logger.info("ℹ️ Permisos de Super Admin restaurados (Contraseña mantenida).");
                }
            }



            // 5. Demo User (Check by email to avoid duplicates)
            if (userRepository.findByEmail("demo@wallet.com") == null) {
                User user = new User();
                user.setName("Usuario Demo");
                user.setEmail("demo@wallet.com");
                user.setPassword(passwordEncoder.encode("password123"));
                user.setRole("ROLE_WORKER");
                user.setEnabled(true);
                
                userRepository.save(user);
                logger.info("Created 'Usuario Demo'");
            }
        };
    }
}
