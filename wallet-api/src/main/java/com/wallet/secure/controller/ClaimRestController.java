package com.wallet.secure.controller;

import com.wallet.secure.entity.Claim;
import com.wallet.secure.repository.ClaimRepository;
import com.wallet.secure.repository.InsuranceRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/claims")
public class ClaimRestController {

    private final ClaimRepository claimRepository;
    private final InsuranceRepository insuranceRepository;

    public ClaimRestController(ClaimRepository claimRepository, InsuranceRepository insuranceRepository) {
        this.claimRepository = claimRepository;
        this.insuranceRepository = insuranceRepository;
    }

    @GetMapping
    public List<Claim> getAllClaims() {
        return claimRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Claim> getClaimById(@PathVariable Long id) {
        return claimRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/insurance/{insuranceId}")
    public ResponseEntity<Claim> createClaim(@PathVariable Long insuranceId, @RequestBody Claim claim, @AuthenticationPrincipal UserDetails userDetails) {
        return insuranceRepository.findById(insuranceId)
                .map(insurance -> {
                    if (!insurance.getUser().getEmail().equals(userDetails.getUsername())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<Claim>build();
                    }
                    claim.setInsurance(insurance);
                    return ResponseEntity.ok(claimRepository.save(claim));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteClaim(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        return claimRepository.findById(id)
                .map(claim -> {
                    if (!claim.getInsurance().getUser().getEmail().equals(userDetails.getUsername())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                    claimRepository.delete(claim);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
