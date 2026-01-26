package com.wallet.secure.repository;

import com.wallet.secure.entity.Insurance;
import com.wallet.secure.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InsuranceRepository extends JpaRepository<Insurance, Long> {
    org.springframework.data.domain.Page<Insurance> findByUser(User user, org.springframework.data.domain.Pageable pageable);
    
    List<Insurance> findByUserAndTitleContainingIgnoreCase(User user, String title);
    List<Insurance> findByUserAndCategory(User user, String category);
    List<Insurance> findByUserAndTitleContainingIgnoreCaseAndCategory(User user, String title, String category);
}
