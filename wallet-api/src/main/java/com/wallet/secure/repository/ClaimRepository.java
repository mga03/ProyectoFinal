package com.wallet.secure.repository;

import com.wallet.secure.entity.Claim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para la gestión de reclamaciones (claims).
 * <p>
 * Proporciona métodos CRUD estándar para la entidad Claim.
 * </p>
 */
@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {
}
