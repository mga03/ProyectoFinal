package com.wallet.secure.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RoleApprovalController {
    @GetMapping("/role-approval")
    public String roleApproval() {
        return "redirect:/"; // Feature disabled
    }
}
