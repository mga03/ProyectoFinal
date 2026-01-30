package com.wallet.secure.controller;

import com.wallet.secure.entity.Insurance;
import com.wallet.secure.entity.User;
import com.wallet.secure.repository.InsuranceRepository;
import com.wallet.secure.repository.UserRepository;
import com.wallet.secure.repository.PaymentRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de pólizas de seguro.
 * <p>
 * Permite a los usuarios autenticados crear, leer, actualizar y eliminar sus propios seguros.
 * También gestiona la adición de beneficiarios a las pólizas.
 * </p>
 */
@RestController
@RequestMapping("/api/insurances")
@CrossOrigin(origins = "*") // In production, restrict to localhost:8080
public class InsuranceRestController {

    private final InsuranceRepository insuranceRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final com.wallet.secure.repository.BeneficiaryRepository beneficiaryRepository;

    public InsuranceRestController(InsuranceRepository insuranceRepository, UserRepository userRepository, PaymentRepository paymentRepository, com.wallet.secure.repository.BeneficiaryRepository beneficiaryRepository) {
        this.insuranceRepository = insuranceRepository;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
        this.beneficiaryRepository = beneficiaryRepository;
    }

    /**
     * Obtiene todos los seguros pertenecientes al usuario autenticado.
     *
     * @param userDetails Detalles del usuario autenticado actual.
     * @return Lista de seguros del usuario.
     */
    @GetMapping
    public List<Insurance> getAllInsurances(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername());
        return insuranceRepository.findByUser(user, org.springframework.data.domain.Pageable.unpaged()).getContent();
    }

    /**
     * Obtiene una póliza de seguro específica por su ID.
     * <p>Valida que el seguro pertenezca al usuario solicitante.</p>
     *
     * @param id Identificador del seguro.
     * @param userDetails Detalles del usuario autenticado.
     * @return ResponseEntity con el seguro si existe y pertenece al usuario, o 404 Not Found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Insurance> getInsurance(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
         Insurance insurance = insuranceRepository.findById(id).orElse(null);
         if (insurance != null && insurance.getUser().getEmail().equals(userDetails.getUsername())) {
             return ResponseEntity.ok(insurance);
         }
         return ResponseEntity.notFound().build();
    }

    /**
     * Crea una nueva póliza de seguro para el usuario autenticado.
     * <p>También genera automáticamente un pago inicial pendiente si hay un monto de prima.</p>
     *
     * @param insurance Objeto Insurance con los datos de la póliza.
     * @param userDetails Detalles del usuario autenticado.
     * @return La póliza de seguro creada.
     */
    @PostMapping
    public Insurance createInsurance(@RequestBody Insurance insurance, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername());
        insurance.setUser(user);
        
        Insurance saved = insuranceRepository.save(insurance);
        
        // Initial Payment Logic (copied from original controller)
        if (insurance.getPremiumAmount() != null) {
            com.wallet.secure.entity.Payment p = new com.wallet.secure.entity.Payment();
            p.setInsurance(saved);
            p.setAmount(insurance.getPremiumAmount());
            p.setPaymentDate(java.time.LocalDate.now());
            p.setStatus(com.wallet.secure.entity.Payment.Status.PENDING);
            paymentRepository.save(p);
        }
        
        return saved;
    }

    /**
     * Actualiza una póliza de seguro existente.
     *
     * @param id Identificador del seguro a actualizar.
     * @param insuranceDetails Nuevos detalles del seguro.
     * @param userDetails Detalles del usuario autenticado.
     * @return ResponseEntity con el seguro actualizado o 404 si no se encuentra/autoriza.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Insurance> updateInsurance(@PathVariable Long id, @RequestBody Insurance insuranceDetails, @AuthenticationPrincipal UserDetails userDetails) {
        Insurance insurance = insuranceRepository.findById(id).orElse(null);
        if (insurance == null || !insurance.getUser().getEmail().equals(userDetails.getUsername())) {
            return ResponseEntity.notFound().build();
        }

        insurance.setTitle(insuranceDetails.getTitle());
        insurance.setPolicyNumber(insuranceDetails.getPolicyNumber());
        insurance.setCategory(insuranceDetails.getCategory());
        insurance.setCompany(insuranceDetails.getCompany());
        insurance.setExpiryDate(insuranceDetails.getExpiryDate());
        insurance.setPremiumAmount(insuranceDetails.getPremiumAmount());
        if (insuranceDetails.getImageUrl() != null) {
            insurance.setImageUrl(insuranceDetails.getImageUrl());
        }

        return ResponseEntity.ok(insuranceRepository.save(insurance));
    }

    /**
     * Elimina una póliza de seguro.
     *
     * @param id Identificador del seguro a eliminar.
     * @param userDetails Detalles del usuario autenticado.
     * @return ResponseEntity con el estado de la operación (OK o Not Found).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInsurance(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        Insurance insurance = insuranceRepository.findById(id).orElse(null);
        if (insurance != null && insurance.getUser().getEmail().equals(userDetails.getUsername())) {
            insuranceRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Añade un beneficiario a una póliza de seguro existente.
     *
     * @param id Identificador del seguro.
     * @param beneficiary Objeto Beneficiary con los datos del beneficiario.
     * @param userDetails Detalles del usuario autenticado.
     * @return ResponseEntity con el beneficiario guardado o 404 si el seguro no se encuentra.
     */
    @PostMapping("/{id}/beneficiaries")
    public ResponseEntity<com.wallet.secure.entity.Beneficiary> addBeneficiary(@PathVariable Long id, @RequestBody com.wallet.secure.entity.Beneficiary beneficiary, @AuthenticationPrincipal UserDetails userDetails) {
        Insurance insurance = insuranceRepository.findById(id).orElse(null);
         if (insurance != null && insurance.getUser().getEmail().equals(userDetails.getUsername())) {
             beneficiary.setInsurance(insurance);
             return ResponseEntity.ok(beneficiaryRepository.save(beneficiary));
         }
         return ResponseEntity.notFound().build();
    }
}
