package com.wallet.secure.controller;

import com.wallet.secure.entity.Ticket;
import com.wallet.secure.entity.User;
import com.wallet.secure.repository.TicketRepository;
import com.wallet.secure.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/support")
public class SupportController {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public SupportController(TicketRepository ticketRepository, UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String support(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            User user = userRepository.findByEmail(userDetails.getUsername());
            model.addAttribute("tickets", ticketRepository.findByUserOrderByCreatedAtDesc(user));
            model.addAttribute("newTicket", new Ticket());
            model.addAttribute("user", user); // For navbar
        }
        return "support";
    }

    @PostMapping("/create")
    public String createTicket(@ModelAttribute Ticket ticket, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            User user = userRepository.findByEmail(userDetails.getUsername());
            ticket.setUser(user);
            ticket.setStatus(Ticket.Status.OPEN);
            ticket.setCreatedAt(java.time.LocalDateTime.now());
            ticketRepository.save(ticket);
        }
        return "redirect:/support?success";
    }
}
