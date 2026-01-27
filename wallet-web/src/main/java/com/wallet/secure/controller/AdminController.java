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

/**
 * Controlador para la gestión administrativa de usuarios.
 * Permite listar y eliminar usuarios, con protecciones para evitar borrar al administrador principal o al propio usuario.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ApiClientService apiClientService;

    public AdminController(ApiClientService apiClientService) {
        this.apiClientService = apiClientService;
    }

    /**
     * Muestra la vista de administración de usuarios.
     * Lista todos los usuarios registrados en el sistema.
     *
     * @param model Modelo para pasar datos a la vista.
     * @return El nombre de la plantilla HTML (admin_users).
     */
    @GetMapping("/users")
    public String adminUsers(Model model) {
        List<User> users = apiClientService.getAllUsers();
        model.addAttribute("users", users);
        return "admin_users";
    }

    /**
     * Elimina un usuario del sistema por su ID.
     * <p>
     * Realiza validaciones de seguridad para impedir la eliminación del Super Admin
     * o del usuario que está actualmente autenticado.
     * </p>
     *
     * @param id ID del usuario a eliminar.
     * @param currentUser Detalles del usuario autenticado actualmente.
     * @param redirectAttributes Atributos para mensajes flash (éxito/error).
     * @return Redirección a la lista de usuarios.
     */
    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, 
                             @AuthenticationPrincipal UserDetails currentUser,
                             RedirectAttributes redirectAttributes) {
        
        try {
            // 1. Obtener usuario a eliminar para verificar
            // Se asume que obtenemos todos y filtramos, ya que no hay un endpoint público getById simple.
            List<User> users = apiClientService.getAllUsers();
            User userToDelete = users.stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .orElse(null);

            if (userToDelete == null) {
                redirectAttributes.addFlashAttribute("error", "Usuario no encontrado.");
                return "redirect:/admin/users";
            }

            // 2. Comprobar si es Super Admin (Protección crítica)
            if ("guarinosmanuel07@gmail.com".equals(userToDelete.getEmail())) {
                redirectAttributes.addFlashAttribute("error", "No se puede eliminar al Super Admin.");
                return "redirect:/admin/users";
            }

            // 3. Comprobar si es el Usuario Actual (Autoprotección)
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