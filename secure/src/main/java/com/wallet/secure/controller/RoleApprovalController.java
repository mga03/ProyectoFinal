package com.wallet.secure.controller;

import com.wallet.secure.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/role-approval")
public class RoleApprovalController {

    private final UserService userService;

    public RoleApprovalController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/approve")
    public String approveRole(@RequestParam("token") String token, Model model) {
        try {
            userService.approveRoleChange(token);
            // Redirigir a login con mensaje de éxito (o página dedicada)
            return "redirect:/login?roleApproved"; 
        } catch (Exception e) {
            model.addAttribute("error", "Error al aprobar rol: " + e.getMessage());
            return "error"; // Usar la vista de error genérica
        }
    }
    
    // Opcional: Rechazo también stateless
    @GetMapping("/reject")
    public String rejectRole(@RequestParam("token") String token, Model model) {
        try {
            userService.rejectRoleChange(token);
            // Redirigir a login con mensaje de rechazo
             return "redirect:/login?roleRejected";
        } catch (Exception e) {
            model.addAttribute("error", "Error al rechazar solicitud: " + e.getMessage());
            return "error";
        }
    }
}
