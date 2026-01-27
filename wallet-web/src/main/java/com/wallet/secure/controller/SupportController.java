package com.wallet.secure.controller;

import com.wallet.secure.dto.Ticket;
import com.wallet.secure.dto.User;
import com.wallet.secure.service.ApiClientService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/support")
public class SupportController {

    private final ApiClientService apiClientService;

    public SupportController(ApiClientService apiClientService) {
        this.apiClientService = apiClientService;
    }

    @GetMapping
    public String support(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) return "redirect:/login";

        User user = apiClientService.getUserByEmail(userDetails.getUsername());
        if (user != null) {
            List<Ticket> tickets = apiClientService.getTicketsByUser(user.getId());
            model.addAttribute("tickets", tickets);
            model.addAttribute("newTicket", new Ticket());
        }
        return "support";
    }

    @PostMapping("/create")
    public String createTicket(@ModelAttribute Ticket ticket, 
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        if (userDetails != null) {
            User user = apiClientService.getUserByEmail(userDetails.getUsername());
            if (user != null) {
                apiClientService.createTicket(user.getId(), ticket);
                redirectAttributes.addFlashAttribute("success", "Ticket creado correctamente.");
            }
        }
        return "redirect:/support";
    }

    // --- Admin Section ---

    @GetMapping("/admin")
    public String adminSupport(Model model) {
        // En un caso real asegurariamos que userDetails sea ADMIN
        List<Ticket> tickets = apiClientService.getAllTickets();
        model.addAttribute("tickets", tickets);
        return "admin_tickets";
    }

    @GetMapping("/admin/tickets/close/{id}")
    public String closeTicket(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        apiClientService.closeTicket(id);
        redirectAttributes.addFlashAttribute("success", "Ticket cerrado correctamente.");
        return "redirect:/support/admin";
    }
}
