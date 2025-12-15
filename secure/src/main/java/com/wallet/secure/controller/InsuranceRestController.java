package com.wallet.secure.controller;

import com.wallet.secure.entity.Insurance;
import com.wallet.secure.entity.User;
import com.wallet.secure.repository.InsuranceRepository;
import com.wallet.secure.repository.UserRepository;
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
    public List<Insurance> getInsurances() {
        Long userId = 1L;
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return List.of();
        }
        return insuranceRepository.findByUser(user, org.springframework.data.domain.Pageable.unpaged()).getContent();
    }

    @PostMapping("/insurances")
    public Insurance saveInsurance(@RequestBody Insurance insurance) {
        Long userId = 1L;
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User with ID 1 not found."));

        insurance.setUser(user);
        return insuranceRepository.save(insurance);
    }

    @DeleteMapping("/insurances/{id}")
    public ResponseEntity<Void> deleteInsurance(@PathVariable Long id) {
        insuranceRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
