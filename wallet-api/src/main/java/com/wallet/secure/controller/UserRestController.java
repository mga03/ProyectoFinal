package com.wallet.secure.controller;

import com.wallet.secure.entity.User;
import com.wallet.secure.repository.UserRepository;
import com.wallet.secure.service.EmailService;
import com.wallet.secure.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de usuarios.
 * <p>
 * Provee endpoints para listar usuarios y realizar operaciones sobre ellos,
 * incluyendo la actualización, borrado lógico y gestión de solicitudes de cambio de rol.
 * </p>
 */
@RestController
@RequestMapping("/api/users")
public class UserRestController {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final UserService userService;

    public UserRestController(UserRepository userRepository, EmailService emailService, UserService userService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.userService = userService;
    }

    /**
     * Obtiene una lista completa de todos los usuarios registrados.
     * <p>Endpoint restringido únicamente a usuarios con rol ADMIN.</p>
     *
     * @return Lista de todos los usuarios.
     */
    @GetMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Busca un usuario específico por su ID.
     *
     * @param id Identificador del usuario.
     * @return ResponseEntity con el usuario encontrado o 404 Not Found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Busca un usuario por su dirección de correo electrónico.
     *
     * @param email Email del usuario a buscar.
     * @return ResponseEntity con el usuario o 404 Not Found.
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        User user = userRepository.findByEmail(email);
        return user != null ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
    }

    /**
     * Actualiza la información personal de un usuario.
     *
     * @param id Identificador del usuario a actualizar.
     * @param userDetails Objeto User con los nuevos datos (nombre, email).
     * @return ResponseEntity con el usuario actualizado.
     */
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setName(userDetails.getName());
                    user.setEmail(userDetails.getEmail());
                    return ResponseEntity.ok(userRepository.save(user));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Elimina un usuario del sistema.
     * <p>Evita explícitamente la eliminación del usuario Administrador principal (ID 1).</p>
     *
     * @param id Identificador del usuario a eliminar.
     * @return ResponseEntity con el resultado de la operación.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (id == 1L) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", "No se puede eliminar al Administrador Principal."));
        }
        return userRepository.findById(id)
                .map(user -> {
                    userRepository.delete(user);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Solicita un cambio de rol para un usuario.
     * <p>
     * Genera un token de cambio de rol y envía una notificación por correo al administrador
     * para que apruebe o rechace la solicitud.
     * </p>
     *
     * @param id Identificador del usuario solicitante.
     * @param newRole El nuevo rol solicitado.
     * @return ResponseEntity indicando que la solicitud ha sido procesada.
     */
    @PostMapping("/{id}/role-request")
    public ResponseEntity<?> requestRoleChange(@PathVariable Long id, @RequestParam String newRole) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setRequestedRole(newRole);
                    user.setRoleChangeToken(java.util.UUID.randomUUID().toString());
                    userRepository.save(user);

                    try {
                        emailService.sendAdminRoleRequest(user.getEmail(), newRole, user.getRoleChangeToken());
                    } catch (Exception e) {
                        System.err.println("Error enviando correo al admin: " + e.getMessage());
                    }

                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Aprueba una solicitud de cambio de rol utilizando un token.
     *
     * @param token El token de aprobación recibido por el administrador.
     * @return ResponseEntity indicando éxito o fallo si el token es inválido.
     */
    @PostMapping("/approve-role")
    public ResponseEntity<?> approveRoleChange(@RequestParam String token) {
        try {
            userService.approveRoleChange(token);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Rechaza una solicitud de cambio de rol utilizando un token.
     *
     * @param token El token de rechazo recibido por el administrador.
     * @return ResponseEntity indicando éxito o fallo si el token es inválido.
     */
    @PostMapping("/reject-role")
    public ResponseEntity<?> rejectRoleChange(@RequestParam String token) {
        try {
            userService.rejectRoleChange(token);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
