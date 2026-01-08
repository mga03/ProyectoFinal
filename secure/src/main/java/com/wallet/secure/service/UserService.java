package com.wallet.secure.service;

import com.wallet.secure.entity.User;
import com.wallet.secure.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    // IMPORTANT: rollbackFor = Exception.class ensures rollback on MessagingException
    @Transactional(rollbackFor = Exception.class)
    public void registerUser(User user, String role, String roleToken) throws Exception {
        // 1. Check if user exists
        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new Exception("El email ya está registrado.");
        }

        // 2. Validate Role Token
        if (!isValidRoleToken(role, roleToken)) {
            throw new Exception("Clave de rol incorrecta.");
        }

        // 3. Prepare User (enabled = false by default in entity, but setting explicitly here)
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(role);
        user.setEnabled(false); 
        user.setVerificationCode(UUID.randomUUID().toString());

        // 4. Save User
        userRepository.save(user);

        // 5. Send Email (If this fails, Transaction rolls back)
        emailService.sendVerificationEmail(user.getEmail(), user.getVerificationCode());
    }

    @Transactional(rollbackFor = Exception.class)
    public void initiatePasswordRecovery(String email) throws Exception {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            String token = UUID.randomUUID().toString();
            user.setResetToken(token);
            userRepository.save(user);
            
            emailService.sendPasswordResetEmail(user.getEmail(), token);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(String token, String newPassword) throws Exception {
        User user = userRepository.findByResetToken(token);
        if (user == null) {
            throw new Exception("Token inválido o expirado.");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        userRepository.save(user);
    }
    
    public boolean verifyUser(String code) {
        User user = userRepository.findByVerificationCode(code);
        if (user == null || user.isEnabled()) {
            return false;
        }
        user.setVerificationCode(null);
        user.setEnabled(true);
        userRepository.save(user);
        return true;
    }

    private boolean isValidRoleToken(String role, String token) {
        switch (role) {
            case "ROLE_ADMIN": return "administrador".equals(token);
            case "ROLE_MANAGER": return "gestor".equals(token);
            case "ROLE_WORKER": return "trabajador".equals(token);
            case "ROLE_COLLABORATOR": return "colaborador".equals(token);
            default: return false;
        }
    }
}
