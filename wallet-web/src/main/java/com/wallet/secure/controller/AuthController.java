package com.wallet.secure.controller;

import com.wallet.secure.dto.User;
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

/**
 * Controlador para la gestión de autenticación y registro de usuarios en la interfaz web.
 */
@Controller
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    /**
     * Muestra la página de inicio de sesión.
     *
     * @return Nombre de la vista login.
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /**
     * Muestra el formulario de registro.
     *
     * @param model Modelo para la vista.
     * @return Nombre de la vista register.
     */
    @GetMapping("/register")
    public String register(Model model) {
        logger.info("Accediendo a /register - Mostrando formulario de registro");
        model.addAttribute("user", new User());
        return "register";
    }

    /**
     * Procesa el registro de un nuevo usuario.
     *
     * @param user Objeto usuario con los datos del formulario.
     * @param result Resultado de la validación.
     * @param model Modelo para la vista.
     * @return Redirección a login si es exitoso, o vuelta al formulario si hay errores.
     */
    @PostMapping("/register/save")
    public String saveUser(@Valid @ModelAttribute("user") User user,
                           BindingResult result,
                           Model model) { // <-- Se han eliminado los @RequestParam de rol

        if (result.hasErrors()) {
            return "register";
        }

        try {
            // Llamamos al servicio sin pasar roles (él asignará el default)
            userService.registerUser(user);
            return "redirect:/login?success";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }
    
    /**
     * Verifica la cuenta del usuario mediante un código enviado por correo.
     *
     * @param code Código de verificación.
     * @return Redirección a login con mensaje de éxito o error.
     */
    @GetMapping("/verify")
    public String verifyAccount(@RequestParam("code") String code) {
        boolean verified = userService.verifyUser(code);
        if (verified) {
             return "redirect:/login?verified";
        } else {
             return "redirect:/login?error";
        }
    }
    
    /**
     * Muestra el formulario de recuperación de contraseña.
     *
     * @return Nombre de la vista forgot_password.
     */
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot_password";
    }
    
    /**
     * Procesa la solicitud de recuperación de contraseña.
     *
     * @param email Correo electrónico del usuario.
     * @param model Modelo para la vista.
     * @return Redirección con mensaje de envío.
     */
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, Model model) {
        try {
            userService.initiatePasswordRecovery(email);
        } catch (Exception e) {
            logger.error("Error en recuperacion: {}", e.getMessage());
        }
        return "redirect:/forgot-password?sent";
    }
    
    /**
     * Muestra el formulario para establecer una nueva contraseña.
     *
     * @param token Token de recuperación.
     * @param model Modelo para la vista.
     * @return Nombre de la vista reset_password.
     */
    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        model.addAttribute("token", token);
        return "reset_password";
    }
    
    /**
     * Procesa el cambio de contraseña.
     *
     * @param token Token de recuperación.
     * @param password Nueva contraseña.
     * @param model Modelo para la vista.
     * @return Redirección a login si es exitoso.
     */
    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("token") String token, 
                                       @RequestParam("password") String password,
                                       Model model) {
        try {
            userService.resetPassword(token, password);
            return "redirect:/login?resetSuccess";
        } catch (Exception e) {
            // IMPRIMIR ERROR EN CONSOLA PARA DIAGNÓSTICO
            System.err.println("ERROR CAMBIANDO PASSWORD: " + e.getMessage());
            e.printStackTrace(); // Ver la traza completa
            
            model.addAttribute("error", "No se pudo cambiar la contraseña: " + e.getMessage());
            return "login"; 
        }
    }
}
