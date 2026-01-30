package com.wallet.secure.repository;

import com.wallet.secure.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para la gestión de pagos.
 * <p>
 * Proporciona operaciones básicas de acceso a datos para la entidad Payment.
 * </p>
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
