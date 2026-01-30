package com.wallet.secure.controller;

import com.wallet.secure.service.ApiClientService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controlador para gestionar la aprobación o rechazo de cambios de rol.
 * Los administradores acceden a estos enlaces desde el correo electrónico.
 */
@Controller
public class RoleApprovalController {

    private final ApiClientService apiClientService;

    public RoleApprovalController(ApiClientService apiClientService) {
        this.apiClientService = apiClientService;
    }

    /**
     * Procesa la aprobación de un cambio de rol mediante token.
     *
     * @param token Token de seguridad de la solicitud.
     * @param model Modelo para la vista de resultado.
     * @return Vista de resultado (role-result.html).
     */
    @GetMapping("/role-approval/approve")
    public String approveRole(@RequestParam String token, Model model) {
        String error = apiClientService.approveRoleChange(token);
        if (error == null) {
            model.addAttribute("message", "La solicitud de rol ha sido APROBADA exitosamente.");
            model.addAttribute("title", "¡Operación Exitosa!");
        } else {
            model.addAttribute("message", "Error: " + error);
            model.addAttribute("title", "Error");
        }
        return "role-result";
    }

    /**
     * Procesa el rechazo de un cambio de rol mediante token.
     *
     * @param token Token de seguridad de la solicitud.
     * @param model Modelo para la vista de resultado.
     * @return Vista de resultado (role-result.html).
     */
    @GetMapping("/role-approval/reject")
    public String rejectRole(@RequestParam String token, Model model) {
        String error = apiClientService.rejectRoleChange(token);
        if (error == null) {
            model.addAttribute("message", "La solicitud de rol ha sido RECHAZADA.");
            model.addAttribute("title", "Operación Completada");
        } else {
            model.addAttribute("message", "Error: " + error);
            model.addAttribute("title", "Error");
        }
        return "role-result";
    }
}
