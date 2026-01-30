package com.wallet.secure.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador para la generación de reportes (Funcionalidad futura).
 * Actualmente redirice a inicio ya que la característica está deshabilitada.
 */
@Controller
public class ReportController {
    @GetMapping("/reports")
    public String reports() {
        return "redirect:/"; // Feature disabled
    }
}
