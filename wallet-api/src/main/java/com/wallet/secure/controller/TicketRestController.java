package com.wallet.secure.controller;

import com.wallet.secure.entity.Ticket;
import com.wallet.secure.entity.User;
import com.wallet.secure.repository.TicketRepository;
import com.wallet.secure.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de tickets de soporte.
 * <p>
 * Permite la creación, seguimiento y cierre de tickets de incidencias
 * reportados por los usuarios.
 * </p>
 */
@RestController
@RequestMapping("/api/tickets")
public class TicketRestController {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public TicketRestController(TicketRepository ticketRepository, UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    /**
     * Obtiene una lista de todos los tickets existentes ordenados cronológicamente.
     *
     * @return Lista completa de tickets.
     */
    @GetMapping
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Obtiene los tickets creados por un usuario específico.
     *
     * @param userId Identificador del usuario.
     * @return ResponseEntity con la lista de tickets del usuario o 404 Not Found.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Ticket>> getTicketsByUser(@PathVariable Long userId) {
        return userRepository.findById(userId)
                .map(user -> ResponseEntity.ok(ticketRepository.findByUserOrderByCreatedAtDesc(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Crea un nuevo ticket asociado a un usuario.
     *
     * @param userId Identificador del usuario que reporta la incidencia.
     * @param ticket Objeto Ticket con el asunto y mensaje.
     * @return ResponseEntity con el ticket creado.
     */
    @PostMapping("/user/{userId}")
    public ResponseEntity<Ticket> createTicket(@PathVariable Long userId, @RequestBody Ticket ticket) {
        return userRepository.findById(userId)
                .map(user -> {
                    ticket.setUser(user);
                    return ResponseEntity.ok(ticketRepository.save(ticket));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Cierra un ticket existente marcándolo como resuelto.
     *
     * @param id Identificador del ticket a cerrar.
     * @return ResponseEntity con el ticket actualizado (estado CLOSED).
     */
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
