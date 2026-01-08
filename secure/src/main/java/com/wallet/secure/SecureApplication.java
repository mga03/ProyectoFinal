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
            // 1. Admin
            if (userRepository.findByEmail("admin@wallet.com") == null) {
                User admin = new User();
                admin.setName("Admin User");
                admin.setEmail("admin@wallet.com");
                admin.setPassword(passwordEncoder.encode("1234"));
                admin.setRole("ROLE_ADMIN");
                admin.setEnabled(true);
                userRepository.save(admin);
                logger.info("Created ADMIN user");
            }

            // 2. Manager
            if (userRepository.findByEmail("manager@wallet.com") == null) {
                User manager = new User();
                manager.setName("Manager User");
                manager.setEmail("manager@wallet.com");
                manager.setPassword(passwordEncoder.encode("1234"));
                manager.setRole("ROLE_MANAGER");
                manager.setEnabled(true);
                userRepository.save(manager);
                logger.info("Created MANAGER user");
            }

            // 3. Worker
            if (userRepository.findByEmail("worker@wallet.com") == null) {
                User worker = new User();
                worker.setName("Worker User");
                worker.setEmail("worker@wallet.com");
                worker.setPassword(passwordEncoder.encode("1234"));
                worker.setRole("ROLE_WORKER");
                worker.setEnabled(true);
                userRepository.save(worker);
                logger.info("Created WORKER user");
            }

            // 4. Collaborator
            if (userRepository.findByEmail("collab@wallet.com") == null) {
                User collab = new User();
                collab.setName("Collaborator User");
                collab.setEmail("collab@wallet.com");
                collab.setPassword(passwordEncoder.encode("1234"));
                collab.setRole("ROLE_COLLABORATOR");
                collab.setEnabled(true);
                userRepository.save(collab);
                logger.info("Created COLLABORATOR user");
            }

            // Check if demo user with ID 1 exists (legacy check, kept for safety)
            if (!userRepository.existsById(1L)) {
                User user = new User();
                user.setName("Usuario Demo");
                user.setEmail("demo@wallet.com");
                user.setPassword(passwordEncoder.encode("password123"));
                user.setRole("ROLE_WORKER");
                user.setEnabled(true);
                
                userRepository.save(user);
                logger.info("Initialized 'Usuario Demo' with ID {}", user.getId());
            }
        };
    }
}
