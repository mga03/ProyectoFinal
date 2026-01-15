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
        if (userDetails != null) {
            User user = userRepository.findByEmail(userDetails.getUsername());
            model.addAttribute("user", user);
        }
        return "profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute User userForm, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByEmail(userDetails.getUsername());
        if (currentUser != null && currentUser.getId().equals(userForm.getId())) {
            currentUser.setName(userForm.getName());
            currentUser.setEmail(userForm.getEmail());
            // Only update password if provided
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
