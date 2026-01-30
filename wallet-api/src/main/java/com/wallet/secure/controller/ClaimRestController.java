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

/**
 * Controlador REST para la gestión de reclamaciones (claims).
 * <p>
 * Gestiona la creación, lectura y eliminación de reclamaciones de seguros.
 * Asegura que las operaciones se realicen sobre seguros pertenecientes al usuario.
 * </p>
 */
@RestController
@RequestMapping("/api/claims")
public class ClaimRestController {

    private final ClaimRepository claimRepository;
    private final InsuranceRepository insuranceRepository;

    public ClaimRestController(ClaimRepository claimRepository, InsuranceRepository insuranceRepository) {
        this.claimRepository = claimRepository;
        this.insuranceRepository = insuranceRepository;
    }

    /**
     * Obtiene todas las reclamaciones registradas en el sistema.
     * <p>Nota: Este endpoint devuelve TODAS las reclamaciones sin filtrar por usuario.</p>
     *
     * @return Lista de todas las reclamaciones.
     */
    @GetMapping
    public List<Claim> getAllClaims() {
        return claimRepository.findAll();
    }

    /**
     * Obtiene una reclamación por su ID.
     *
     * @param id Identificador de la reclamación.
     * @return ResponseEntity con la reclamación o 404 Not Found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Claim> getClaimById(@PathVariable Long id) {
        return claimRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Crea una nueva reclamación para un seguro específico.
     * <p>Verifica que el seguro pertenezca al usuario autenticado antes de crear la reclamación.</p>
     *
     * @param insuranceId Identificador del seguro asociado.
     * @param claim Objeto Claim con los detalles del siniestro.
     * @param userDetails Detalles del usuario autenticado.
     * @return ResponseEntity con la reclamación creada o error si el seguro no pertenece al usuario.
     */
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

    /**
     * Elimina una reclamación existente.
     * <p>Verifica que la reclamación pertenezca a un seguro del usuario autenticado.</p>
     *
     * @param id Identificador de la reclamación a eliminar.
     * @param userDetails Detalles del usuario autenticado.
     * @return ResponseEntity con el estado de la operación.
     */
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
