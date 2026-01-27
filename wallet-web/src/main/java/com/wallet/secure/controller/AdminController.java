package com.wallet.secure.controller;

import com.wallet.secure.dto.User;
import com.wallet.secure.service.ApiClientService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
        List<User> users = apiClientService.getAllUsers();
        model.addAttribute("users", users);
        return "admin_users";
    }

    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, 
                             @AuthenticationPrincipal UserDetails currentUser,
                             RedirectAttributes redirectAttributes) {
        
        // 1. Obtener usuario a eliminar para verificar
        // (En un caso real, podrías necesitar un endpoint getById en ApiClientService, 
        //  o buscarlo en la lista si la tienes cacheada, pero mejor volver a pedirlo)
        // Como ApiClientService tiene un getAllUsers, podríamos filtrar, o añadir getUserById.
        // Asumiremos que getUserById es viable o iteramos sobre getAllUsers si la lista es pequeña.
        
        // Mejor opción: delegar la lógica de seguridad al endpoint de borrado o comprobar aquí si es posible.
        // Dado que no tengo getUserById público fácil en ApiClientService (sí tengo getInsuranceById),
        // voy a asumir que puedo llamar a getAllUsers y filtrar.
        
        try {
            List<User> users = apiClientService.getAllUsers();
            User userToDelete = users.stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .orElse(null);

            if (userToDelete == null) {
                redirectAttributes.addFlashAttribute("error", "Usuario no encontrado.");
                return "redirect:/admin/users";
            }

            // 2. Comprobar si es Super Admin
            if ("guarinosmanuel07@gmail.com".equals(userToDelete.getEmail())) {
                redirectAttributes.addFlashAttribute("error", "No se puede eliminar al Super Admin.");
                return "redirect:/admin/users";
            }

            // 3. Comprobar si es el Usuario Actual
            if (currentUser != null && currentUser.getUsername().equals(userToDelete.getEmail())) {
                redirectAttributes.addFlashAttribute("error", "No puedes eliminar tu propia cuenta mientras estás logueado.");
                return "redirect:/admin/users";
            }

            // 4. Proceder al borrado
            apiClientService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", "Usuario eliminado correctamente.");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error eliminando usuario: " + e.getMessage());
        }

        return "redirect:/admin/users";
    }
}