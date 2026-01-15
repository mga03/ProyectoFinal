package com.wallet.secure.repository;

import com.wallet.secure.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
    
    User findByVerificationCode(String verificationCode);
    
    User findByResetToken(String resetToken);
    
    long countByRole(String role);

    // Nuevo para cambio de rol stateless
    User findByRoleChangeToken(String roleChangeToken);
}
