package com.wallet.secure.repository;

import com.wallet.secure.entity.Ticket;
import com.wallet.secure.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByUserOrderByCreatedAtDesc(User user);
    List<Ticket> findAllByOrderByCreatedAtDesc();
}
