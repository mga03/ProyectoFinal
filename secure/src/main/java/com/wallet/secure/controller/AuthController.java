package com.wallet.secure.controller;

import com.wallet.secure.entity.User;
import com.wallet.secure.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register/save")
    public String saveUser(@ModelAttribute("user") User user,
                           @org.springframework.web.bind.annotation.RequestParam String role,
                           @org.springframework.web.bind.annotation.RequestParam String roleToken) {
        
        if (userRepository.findByEmail(user.getEmail()) != null) {
            return "redirect:/register?error";
        }

        // Validate Role Token
        boolean isValid = false;
        switch (role) {
            case "ROLE_ADMIN":
                if ("administrador".equals(roleToken)) isValid = true;
                break;
            case "ROLE_MANAGER":
                if ("gestor".equals(roleToken)) isValid = true;
                break;
            case "ROLE_WORKER":
                if ("trabajador".equals(roleToken)) isValid = true;
                break;
            case "ROLE_COLLABORATOR":
                if ("colaborador".equals(roleToken)) isValid = true;
                break;
            default:
                isValid = false;
        }

        if (!isValid) {
            return "redirect:/register?errorToken";
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(role);
        userRepository.save(user);
        return "redirect:/login?success";
    }
}
