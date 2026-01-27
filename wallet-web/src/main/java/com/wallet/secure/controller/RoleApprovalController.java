package com.wallet.secure.controller;

import com.wallet.secure.service.ApiClientService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RoleApprovalController {

    private final ApiClientService apiClientService;

    public RoleApprovalController(ApiClientService apiClientService) {
        this.apiClientService = apiClientService;
    }

    @GetMapping("/role-approval/approve")
    public String approveRole(@RequestParam String token, Model model) {
        boolean success = apiClientService.approveRoleChange(token);
        if (success) {
            model.addAttribute("message", "La solicitud de rol ha sido APROBADA exitosamente.");
            model.addAttribute("title", "¡Operación Exitosa!");
        } else {
            model.addAttribute("message", "No se pudo aprobar la solicitud. El token puede ser inválido o haber expirado.");
            model.addAttribute("title", "Error");
        }
        return "role-result";
    }

    @GetMapping("/role-approval/reject")
    public String rejectRole(@RequestParam String token, Model model) {
        boolean success = apiClientService.rejectRoleChange(token);
        if (success) {
            model.addAttribute("message", "La solicitud de rol ha sido RECHAZADA.");
            model.addAttribute("title", "Operación Completada");
        } else {
            model.addAttribute("message", "No se pudo rechazar la solicitud. El token puede ser inválido o haber expirado.");
            model.addAttribute("title", "Error");
        }
        return "role-result";
    }
}
