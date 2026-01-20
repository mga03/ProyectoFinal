package com.wallet.secure.controller;

import com.wallet.secure.entity.User;
import com.wallet.secure.repository.UserRepository;
import com.wallet.secure.service.EmailService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final com.wallet.secure.service.UserService userService;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService, com.wallet.secure.service.UserService userService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.userService = userService;
    }

    @GetMapping("/profile")
    public String profile(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        // 1. SEGURIDAD: Si no hay credenciales, fuera.
        if (userDetails == null) {
            return "redirect:/login";
        }

        // 2. CONSISTENCIA: Buscar el usuario real en la BD
        User user = userRepository.findByEmail(userDetails.getUsername());
        
        // 3. CASO "ZOMBIE": Si la sesión existe pero el usuario fue borrado de la BD...
        if (user == null) {
            System.err.println("⚠️ Detectada sesión huérfana para: " + userDetails.getUsername());
            // Redirigimos al logout para limpiar esa sesión corrupta
            return "redirect:/login?logout"; 
        }

        // 4. Todo correcto: Cargamos el perfil
        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@jakarta.validation.Valid @ModelAttribute("user") User userForm,
                                org.springframework.validation.BindingResult result,
                                @AuthenticationPrincipal UserDetails userDetails,
                                Model model) {
        
        // 1. Recuperar el usuario real de la BD para no perder datos (id, rol, pass)
        User currentUser = userRepository.findByEmail(userDetails.getUsername());
        
        // 2. Comprobar errores de validación (ej: móvil "123")
        if (result.hasErrors()) {
            // Si falla, volvemos a la vista 'profile' mostrando los errores
            // Restauramos el ID y Rol del usuario original para evitar errores en la vista
            if (currentUser != null) {
                userForm.setId(currentUser.getId());
                userForm.setRole(currentUser.getRole());
                userForm.setEmail(currentUser.getEmail()); // El email no se suele cambiar aquí o se valida aparte
            }
            return "profile"; 
        }

        // 3. Si todo es válido, actualizamos los campos
        if (currentUser != null && currentUser.getId().equals(userForm.getId())) {
            currentUser.setName(userForm.getName());
            // currentUser.setEmail(userForm.getEmail()); // Opcional: si permites cambiar email
            
            // ¡IMPORTANTE! Actualizar el móvil, que antes faltaba
            currentUser.setMobile(userForm.getMobile());
            
            // Solo actualizamos contraseña si el usuario escribió algo nuevo
            if (userForm.getPassword() != null && !userForm.getPassword().isEmpty()) {
                currentUser.setPassword(passwordEncoder.encode(userForm.getPassword()));
            }
            
            userRepository.save(currentUser);
        }
        return "redirect:/profile?success";
    }

    @PostMapping("/profile/request-role")
    public String requestRoleChange(@RequestParam String desiredRole, 
                                    @AuthenticationPrincipal UserDetails currentUser,
                                    RedirectAttributes redirectAttributes) {
        try {
            // Usamos el servicio de usuario para generar token y enviar correo
            userService.requestRoleChange(currentUser.getUsername(), desiredRole);
            
            redirectAttributes.addFlashAttribute("msg", "Solicitud enviada correctamente al Administrador.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error enviando solicitud: " + e.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/profile/delete")
    public String deleteProfile(@org.springframework.web.bind.annotation.RequestParam("confirmation") String confirmation, 
                                @AuthenticationPrincipal UserDetails userDetails,
                                jakarta.servlet.http.HttpServletRequest request,
                                jakarta.servlet.http.HttpServletResponse response) {
        
        if (!"eliminar".equals(confirmation)) {
            return "redirect:/profile?error=invalid_confirmation";
        }

        User user = userRepository.findByEmail(userDetails.getUsername());
        if (user != null) {
            userRepository.delete(user);
            
            // Clear session manually
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
            new org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler().logout(request, response, null);
        }

        return "redirect:/login?deleted";
    }
}
