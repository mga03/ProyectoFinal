package com.wallet.secure.repository;

import com.wallet.secure.entity.Ticket;
import com.wallet.secure.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repositorio para la gestión de tickets de soporte.
 * <p>
 * Facilita la recuperación de tickets ordenados por fecha de creación,
 * tanto para usuarios individuales como para administradores (todos los tickets).
 * </p>
 */
@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    /**
     * Busca todos los tickets de un usuario ordenados por fecha de creación descendente (más recientes primero).
     *
     * @param user El usuario creador de los tickets.
     * @return Lista de tickets del usuario ordenados.
     */
    List<Ticket> findByUserOrderByCreatedAtDesc(User user);

    /**
     * Recupera todos los tickets del sistema ordenados por fecha de creación descendente.
     *
     * @return Lista completa de tickets ordenados.
     */
    List<Ticket> findAllByOrderByCreatedAtDesc();
}
