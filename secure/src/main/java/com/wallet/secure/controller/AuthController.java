package com.wallet.secure.controller;

import com.wallet.secure.entity.User;
import com.wallet.secure.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

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
    public String saveUser(@Valid @ModelAttribute("user") User user,
                           BindingResult result,
                           @RequestParam String role,
                           @RequestParam String roleToken,
                           Model model) {

        if (result.hasErrors()) {
            return "register";
        }
        
        try {
            userService.registerUser(user, role, roleToken);
            logger.info("Usuario registrado correctamente (pendiente verificacion): {}", user.getEmail());
            return "redirect:/login?verify";
        } catch (Exception e) {
            logger.error("Error en registro: {}", e.getMessage());
            // Show clear error message to user
            model.addAttribute("error", "Error en el registro: " + e.getMessage());
            return "register";
        }
    }
    
    @GetMapping("/verify")
    public String verifyAccount(@RequestParam("code") String code) {
        boolean verified = userService.verifyUser(code);
        if (verified) {
             return "redirect:/login?verified";
        } else {
             return "redirect:/login?error";
        }
    }
    
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot_password";
    }
    
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, Model model) {
        try {
            userService.initiatePasswordRecovery(email);
        } catch (Exception e) {
            logger.error("Error en recuperacion: {}", e.getMessage());
        }
        return "redirect:/forgot-password?sent";
    }
    
    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        model.addAttribute("token", token);
        return "reset_password";
    }
    
    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("token") String token, 
                                       @RequestParam("password") String password,
                                       Model model) {
        try {
            userService.resetPassword(token, password);
            return "redirect:/login?resetSuccess";
        } catch (Exception e) {
            model.addAttribute("error", "Error restableciendo contraseña: " + e.getMessage());
            return "login"; 
        }
    }
}
