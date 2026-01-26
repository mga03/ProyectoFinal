package com.wallet.secure.controller;

import com.wallet.secure.dto.User;
import com.wallet.secure.service.ApiClientService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ApiClientService apiClientService;

    public AdminController(ApiClientService apiClientService) {
        this.apiClientService = apiClientService;
    }

    @GetMapping("/users")
    public String adminUsers(Model model) {
        // 1. Obtener lista de usuarios desde la API
        List<User> users = apiClientService.getAllUsers();
        
        model.addAttribute("users", users);
        return "admin_users"; // Asegúrate de tener admin_users.html en templates
    }

    // Si tenías tickets también:
    /*
    @GetMapping("/tickets")
    public String adminTickets(Model model) {
        // List<Ticket> tickets = apiClientService.getAllTickets();
        // model.addAttribute("tickets", tickets);
        return "admin_tickets";
    }
    */
}