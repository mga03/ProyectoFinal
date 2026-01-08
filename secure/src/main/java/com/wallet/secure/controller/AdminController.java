package com.wallet.secure.controller;

import com.wallet.secure.entity.User;
import com.wallet.secure.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private final UserRepository userRepository;

    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
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
