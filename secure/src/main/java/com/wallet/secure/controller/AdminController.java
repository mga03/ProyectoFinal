package com.wallet.secure.controller;

import com.wallet.secure.entity.User;
import com.wallet.secure.repository.UserRepository;
import com.wallet.secure.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final com.wallet.secure.service.UserService userService;

    public AdminController(UserRepository userRepository, EmailService emailService, com.wallet.secure.service.UserService userService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.userService = userService;
    }

    // Solo el ADMIN puede acceder a estos links (seguridad)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/role-action")
    public String handleRoleAction(@RequestParam String action, 
                                   @RequestParam String email, 
                                   @RequestParam(required = false) String role,
                                   RedirectAttributes redirectAttributes) {
        
        // 1. LOGS PARA DIAGNÓSTICO (Míralos en la consola si falla)
        System.out.println("⚡ ADMIN ACTION: " + action + " | Email: " + email + " | Role: " + role);

        try {
            System.out.println("🔍 Buscando usuario: " + email);
            User user = userRepository.findByEmail(email);
            
            if (user == null) {
                System.err.println("❌ Usuario no encontrado: " + email);
                throw new Exception("El usuario con email " + email + " no existe.");
            }
            System.out.println("✅ Usuario encontrado. Rol actual: " + user.getRole());

            if ("approve".equals(action)) {
                if (role == null || role.isEmpty()) {
                    throw new Exception("El rol a asignar está vacío.");
                }

                // PROTECCIÓN: No degradar al último administrador
                if ("ROLE_ADMIN".equals(user.getRole()) && !"ROLE_ADMIN".equals(role)) {
                    long adminCount = userRepository.countByRole("ROLE_ADMIN");
                    System.out.println("🛡️ Verificando admins restantes. Total actual: " + adminCount);
                    if (adminCount <= 1) {
                        throw new Exception("⚠️ PREVENCIÓN DE BLOQUEO: No puedes quitarle el rol de Administrador al único Administrador del sistema.");
                    }
                }

                user.setRole(role);
                User saved = userRepository.save(user);
                System.out.println("💾 Usuario guardado. Nuevo rol en BD: " + saved.getRole());
                
                // Avisar al usuario por email
                try {
                    emailService.sendRoleStatusEmail(email, "APROBADO", role);
                } catch (Exception e) {
                    System.err.println("⚠️ Error enviando email de confirmación: " + e.getMessage());
                    // No relanzamos para no interrumpir el flujo exitoso
                }
                
                redirectAttributes.addFlashAttribute("msg", "✅ ¡Éxito! Rol de " + user.getName() + " actualizado a " + role);
                
            } else if ("reject".equals(action)) {
                // Avisar al usuario del rechazo
                emailService.sendRoleStatusEmail(email, "DENEGADO", "");
                redirectAttributes.addFlashAttribute("error", "❌ Solicitud rechazada correctamente.");
            }

        } catch (Exception e) {
            // Imprimir el error real en la consola para poder arreglarlo
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error procesando solicitud: " + e.getMessage());
        }
        
        // Redirigir siempre al inicio para evitar conflictos de vista
        return "redirect:/index";
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "admin_users";
    }

    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        Optional<User> userOptional = userRepository.findById(id);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            
            // Protect last admin
            if ("ROLE_ADMIN".equals(user.getRole())) {
                 long adminCount = userRepository.countByRole("ROLE_ADMIN");
                 if (adminCount <= 1) {
                     logger.warn("Intento de eliminar al ultimo administrador: {}", user.getEmail());
                     return "redirect:/admin/users?errorLastAdmin";
                 }
            }
            
            userRepository.deleteById(id);
            logger.info("Usuario eliminado por admin ID {}: {}", id, user.getEmail());
        } else {
             logger.warn("Intento de eliminar usuario inexistente ID: {}", id);
        }

        return "redirect:/admin/users?success";
    }
}
