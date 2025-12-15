package com.wallet.secure;

import com.wallet.secure.entity.User;
import com.wallet.secure.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SecureApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecureApplication.class, args);
    }

    @Bean
    public CommandLineRunner initData(UserRepository userRepository) {
        return args -> {
            // Check if user with ID 1 exists
            if (!userRepository.existsById(1L)) {
                User user = new User();
                // Ensure ID strategy allows us to assume 1 will be next or we rely on the DB
                // Since it's a demo, standard Identity generation usually starts at 1
                // We create a user and let JPA handle the ID, but for the logic relying on ID 1
                // we assume this is the first user.
                user.setName("Usuario Demo");
                user.setEmail("demo@wallet.com");
                user.setPassword("password123");
                
                userRepository.save(user);
                System.out.println("Initialized 'Usuario Demo' with ID " + user.getId());
            }
        };
    }
}
