package com.wallet.secure.controller;

import com.wallet.secure.dto.User;
import com.wallet.secure.service.ApiClientService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador para la gestión del perfil de usuario en el frontend.
 * Permite visualizar el perfil, actualizar datos, solicitar cambios de rol y eliminar la cuenta.
 */
@Controller
public class UserController {

    private final ApiClientService apiClientService;

    public UserController(ApiClientService apiClientService) {
        this.apiClientService = apiClientService;
    }

    /**
     * Muestra la página de perfil del usuario.
     *
     * @param model Modelo de la vista.
     * @param userDetails Detalles del usuario autenticado.
     * @return Nombre de la vista de perfil.
     */
    @GetMapping("/profile")
    public String profile(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        User user = apiClientService.getUserByEmail(userDetails.getUsername());
        
        if (user == null) {
            return "redirect:/login?logout"; 
        }

        model.addAttribute("user", user);
        return "profile";
    }

    /**
     * Actualiza la información del perfil del usuario.
     *
     * @param userForm Objeto User con datos nuevos.
     * @param result Resultado de validación.
     * @param userDetails Detalles de autenticación.
     * @param model Modelo para la vista en caso de error.
     * @return Redirección a perfil con mensaje de éxito o retorno a vista con errores.
     */
    @PostMapping("/profile/update")
    public String updateProfile(@jakarta.validation.Valid @ModelAttribute("user") User userForm,
                                org.springframework.validation.BindingResult result,
                                @AuthenticationPrincipal UserDetails userDetails,
                                Model model) {
        
        User currentUser = apiClientService.getUserByEmail(userDetails.getUsername());
        
        if (result.hasErrors()) {
            if (currentUser != null) {
                userForm.setId(currentUser.getId());
                userForm.setRole(currentUser.getRole());
                userForm.setEmail(currentUser.getEmail());
            }
            return "profile"; 
        }

        if (currentUser != null && currentUser.getId().equals(userForm.getId())) {
            // We pass the updated form data to API. API should handle selective updates or we merge here.
            currentUser.setName(userForm.getName());
            currentUser.setMobile(userForm.getMobile());
            if (userForm.getPassword() != null && !userForm.getPassword().isEmpty()) {
                currentUser.setPassword(userForm.getPassword()); // Sending plain text, API hashes (if updated logic is there)
            }
            apiClientService.updateUser(currentUser);
        }
        return "redirect:/profile?success";
    }

    /**
     * Procesa la solicitud de cambio de rol de un usuario.
     *
     * @param desiredRole Rol que el usuario desea obtener.
     * @param userDetails Detalles del usuario autenticado.
     * @param redirectAttributes Atributos para mensajes flash.
     * @return Redirección a perfil.
     */
    @PostMapping("/profile/request-role")
    public String requestRoleChange(@RequestParam String desiredRole, 
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    RedirectAttributes redirectAttributes) {
        if (userDetails != null) {
            User user = apiClientService.getUserByEmail(userDetails.getUsername());
            if (user != null) {
                apiClientService.requestRoleChange(user.getId(), desiredRole);
                redirectAttributes.addFlashAttribute("msg", "Solicitud enviada exitosamente.");
            }
        }
        return "redirect:/profile";
    }

    // @PostMapping("/profile/delete") - Fix compilation by using RequestParam correctly
    /**
     * Elimina la cuenta del usuario actual tras confirmación.
     *
     * @param confirmation Palabra clave de confirmación ("eliminar").
     * @param userDetails Detalles del usuario usuario.
     * @param request Petición HTTP.
     * @param response Respuesta HTTP.
     * @return Redirección a login tras eliminar cuenta y cerrar sesión.
     */
    @PostMapping("/profile/delete")
    public String deleteProfile(@RequestParam("confirmation") String confirmation, 
                                @AuthenticationPrincipal UserDetails userDetails,
                                jakarta.servlet.http.HttpServletRequest request,
                                jakarta.servlet.http.HttpServletResponse response) {
        
        if (!"eliminar".equals(confirmation)) {
            return "redirect:/profile?error=invalid_confirmation";
        }

        User user = apiClientService.getUserByEmail(userDetails.getUsername());
        if (user != null) {
            apiClientService.deleteUser(user.getId());
            
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
            new org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler().logout(request, response, null);
        }

        return "redirect:/login?deleted";
    }
}
