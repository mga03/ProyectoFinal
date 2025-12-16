package com.wallet.secure.controller;

import com.wallet.secure.entity.Insurance;
import com.wallet.secure.entity.User;
import com.wallet.secure.repository.InsuranceRepository;
import com.wallet.secure.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class InsuranceRestController {

    private final InsuranceRepository insuranceRepository;
    private final UserRepository userRepository;

    public InsuranceRestController(InsuranceRepository insuranceRepository, UserRepository userRepository) {
        this.insuranceRepository = insuranceRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/insurances")
    public List<Insurance> getInsurances(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername());
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return insuranceRepository.findByUser(user, org.springframework.data.domain.Pageable.unpaged()).getContent();
    }

    @PostMapping("/insurances")
    public Insurance saveInsurance(@RequestBody Insurance insurance, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername());
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        insurance.setUser(user);
        return insuranceRepository.save(insurance);
    }

    @DeleteMapping("/insurances/{id}")
    public ResponseEntity<Void> deleteInsurance(@PathVariable Long id) {
        insuranceRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
