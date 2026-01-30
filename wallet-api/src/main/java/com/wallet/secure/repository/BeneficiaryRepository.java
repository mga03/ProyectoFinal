package com.wallet.secure.repository;

import com.wallet.secure.entity.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para la gestión de beneficiarios.
 * <p>
 * Permite realizar operaciones CRUD sobre la entidad Beneficiary.
 * </p>
 */
@Repository
public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {
}
