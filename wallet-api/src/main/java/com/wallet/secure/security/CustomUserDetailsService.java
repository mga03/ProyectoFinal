package com.wallet.secure.security;

import com.wallet.secure.entity.User;
import com.wallet.secure.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            System.err.println("❌ LOGIN ERROR: Usuario no encontrado: " + email);
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        System.out.println("🔐 LOGIN ATTEMPT: " + email + " | Role in DB: " + user.getRole());
        
        // Ensure role has ROLE_ prefix if missing (safety check)
        String roleName = user.getRole();
        if (!roleName.startsWith("ROLE_")) {
             System.out.println("⚠️ WARN: Role " + roleName + " missing prefix. Adding ROLE_ temporary.");
             roleName = "ROLE_" + roleName;
        }

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.isEnabled(),
                true,
                true,
                true,
                Collections.singletonList(new SimpleGrantedAuthority(roleName))
        );
    }
}
