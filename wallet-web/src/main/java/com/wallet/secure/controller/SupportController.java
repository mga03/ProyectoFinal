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

/**
 * Controlador para la gestión de tickets de soporte técnico.
 * Permite a los usuarios crear tickets y ver su estado, y a los administradores gestionarlos.
 */
@Controller
@RequestMapping("/support")
public class SupportController {

    private final ApiClientService apiClientService;

    public SupportController(ApiClientService apiClientService) {
        this.apiClientService = apiClientService;
    }

    /**
     * Muestra la lista de tickets del usuario y el formulario para crear uno nuevo.
     *
     * @param userDetails Detalles del usuario usuario autenticado.
     * @param model Modelo para la vista.
     * @return Vista de soporte (support.html) o redirección a login.
     */
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

    /**
     * Crea un nuevo ticket de soporte.
     *
     * @param ticket Objeto Ticket con los datos del formulario.
     * @param userDetails Usuario autenticado.
     * @param redirectAttributes Mensajes flash.
     * @return Redirección a la página de soporte.
     */
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

    /**
     * Muestra todos los tickets para administración.
     *
     * @param model Modelo de la vista.
     * @return Vista de tickets de administración.
     */
    @GetMapping("/admin")
    public String adminSupport(Model model) {
        // En un caso real asegurariamos que userDetails sea ADMIN
        List<Ticket> tickets = apiClientService.getAllTickets();
        model.addAttribute("tickets", tickets);
        return "admin_tickets";
    }

    /**
     * Cierra un ticket específico.
     *
     * @param id ID del ticket.
     * @param redirectAttributes Mensajes flash.
     * @return Redirección a la lista de tickets de admin.
     */
    @GetMapping("/admin/tickets/close/{id}")
    public String closeTicket(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        apiClientService.closeTicket(id);
        redirectAttributes.addFlashAttribute("success", "Ticket cerrado correctamente.");
        return "redirect:/support/admin";
    }
}
