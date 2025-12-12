package com.wallet.secure.controller;

import com.wallet.secure.entity.User;
import com.wallet.secure.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
}
