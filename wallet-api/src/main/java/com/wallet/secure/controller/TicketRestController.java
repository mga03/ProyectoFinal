package com.wallet.secure.controller;

import com.wallet.secure.entity.Ticket;
import com.wallet.secure.entity.User;
import com.wallet.secure.repository.TicketRepository;
import com.wallet.secure.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketRestController {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public TicketRestController(TicketRepository ticketRepository, UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAllByOrderByCreatedAtDesc();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Ticket>> getTicketsByUser(@PathVariable Long userId) {
        return userRepository.findById(userId)
                .map(user -> ResponseEntity.ok(ticketRepository.findByUserOrderByCreatedAtDesc(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<Ticket> createTicket(@PathVariable Long userId, @RequestBody Ticket ticket) {
        return userRepository.findById(userId)
                .map(user -> {
                    ticket.setUser(user);
                    return ResponseEntity.ok(ticketRepository.save(ticket));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/close")
    public ResponseEntity<Ticket> closeTicket(@PathVariable Long id) {
        return ticketRepository.findById(id)
                .map(ticket -> {
                    ticket.setStatus(Ticket.Status.CLOSED);
                    return ResponseEntity.ok(ticketRepository.save(ticket));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
