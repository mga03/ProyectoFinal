package com.wallet.secure.service;

import com.wallet.secure.dto.Insurance;
import com.wallet.secure.dto.Ticket;
import com.wallet.secure.dto.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;

/**
 * Servicio cliente para interactuar con la API REST del backend (wallet-api).
 * Gestiona todas las llamadas HTTP para usuarios, seguros, tickets y autenticación.
 */
@Service
public class ApiClientService {

    private final RestTemplate restTemplate = new RestTemplate();
    
    @org.springframework.beans.factory.annotation.Value("${app.api.url:http://localhost:8081/api}")
    private String API_URL;

    private static final Logger logger = LoggerFactory.getLogger(ApiClientService.class);

    /**
     * Genera los encabezados HTTP incluyendo el Token de identificación (X-Auth-User).
     * Este encabezado es necesario para que la API identifique al usuario que realiza la petición.
     *
     * @return HttpHeaders configurados con el Content-Type y el usuario autenticado.
     */
    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            
            // Lógica para extraer el email independientemente del tipo de objeto principal
            if (principal instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) principal;
                headers.set("X-Auth-User", userDetails.getUsername());
            } else if (principal instanceof User) {
                User u = (User) principal;
                headers.set("X-Auth-User", u.getEmail());
            } else if (principal instanceof String) {
                headers.set("X-Auth-User", (String) principal);
            }
        }
        return headers;
    }

    // --- Autenticación y Usuarios ---

    /**
     * Verifica las credenciales de inicio de sesión contra la API.
     *
     * @param email Correo electrónico del usuario.
     * @param password Contraseña del usuario.
     * @return El objeto User si las credenciales son válidas, null en caso contrario.
     */
    public User verifyLogin(String email, String password) {
        try {
            ResponseEntity<User> response = restTemplate.postForEntity(
                API_URL + "/auth/login", 
                Map.of("email", email, "password", password), 
                User.class
            );
            return response.getBody();
        } catch (Exception e) {
            logger.error("Login fallido: " + e.getMessage());
            return null;
        }
    }

    /**
     * Obtiene la lista de todos los usuarios registrados (Solo para administradores).
     *
     * @return Lista de objetos User.
     */
    public List<User> getAllUsers() {
        try {
            ResponseEntity<List<User>> response = restTemplate.exchange(
                API_URL + "/users", 
                HttpMethod.GET,
                new HttpEntity<>(getHeaders()),
                new ParameterizedTypeReference<List<User>>() {}
            );
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error obteniendo usuarios: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param user Objeto User con los datos de registro.
     */
    public void registerUser(User user) {
        restTemplate.postForEntity(API_URL + "/auth/register", user, Map.class);
    }

    /**
     * Verifica el código de activación de cuenta.
     *
     * @param code Código de verificación.
     * @return true si la verificación es exitosa.
     */
    public boolean verifyUser(String code) {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(API_URL + "/auth/verify?code={code}", Map.class, code);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Obtiene los detalles de un usuario por su correo electrónico.
     *
     * @param email Correo del usuario.
     * @return El objeto User correspondiente.
     */
    public User getUserByEmail(String email) {
        try {
            // Nota: Enviamos el propio email en el header para "auto-autorizarnos" 
            // y permitir que la API devuelva los datos del usuario.
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Auth-User", email); 
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<User> response = restTemplate.exchange(
                API_URL + "/users/email/" + email, 
                HttpMethod.GET, 
                entity, 
                User.class
            );
            return response.getBody();
        } catch (Exception e) {
            logger.warn("No se pudo obtener el usuario por email: " + email);
            return null;
        }
    }

    // --- Seguros (Insurance) ---

    /**
     * Obtiene todos los seguros disponibles.
     *
     * @return Lista de objetos Insurance.
     */
    public List<Insurance> getAllInsurances() {
        try {
            ResponseEntity<List<Insurance>> response = restTemplate.exchange(
                API_URL + "/insurances",
                HttpMethod.GET,
                new HttpEntity<>(getHeaders()),
                new ParameterizedTypeReference<List<Insurance>>() {}
            );
            return response.getBody();
        } catch (Exception e) {
            return List.of(); // Retorna lista vacía en caso de error para no romper la interfaz
        }
    }

    /**
     * Obtiene un seguro específico por su ID.
     *
     * @param id ID del seguro.
     * @return El objeto Insurance encontrado o null.
     */
    public Insurance getInsuranceById(Long id) {
        try {
            return restTemplate.exchange(
                API_URL + "/insurances/" + id,
                HttpMethod.GET,
                new HttpEntity<>(getHeaders()),
                Insurance.class
            ).getBody();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Guarda (crea o actualiza) un seguro.
     *
     * @param insurance Objeto Insurance a guardar.
     */
    public void saveInsurance(Insurance insurance) {
        if (insurance.getId() == null) {
            restTemplate.postForEntity(API_URL + "/insurances", new HttpEntity<>(insurance, getHeaders()), Insurance.class);
        } else {
            restTemplate.exchange(API_URL + "/insurances/" + insurance.getId(), HttpMethod.PUT, new HttpEntity<>(insurance, getHeaders()), Insurance.class);
        }
    }

    /**
     * Elimina un seguro por su ID.
     *
     * @param id ID del seguro a eliminar.
     */
    public void deleteInsurance(Long id) {
        restTemplate.exchange(API_URL + "/insurances/" + id, HttpMethod.DELETE, new HttpEntity<>(getHeaders()), Void.class);
    }
    
    /**
     * Guarda una reclamación asociada a un seguro.
     *
     * @param claim Objeto Claim.
     * @param insuranceId ID del seguro asociado.
     */
    public void saveClaim(com.wallet.secure.dto.Claim claim, Long insuranceId) {
        restTemplate.postForEntity(API_URL + "/claims/insurance/" + insuranceId, new HttpEntity<>(claim, getHeaders()), com.wallet.secure.dto.Claim.class);
    }
    
    /**
     * Guarda un beneficiario asociado a un seguro.
     *
     * @param beneficiary Objeto Beneficiary.
     * @param insuranceId ID del seguro asociado.
     */
    public void saveBeneficiary(com.wallet.secure.dto.Beneficiary beneficiary, Long insuranceId) {
        restTemplate.postForEntity(API_URL + "/insurances/" + insuranceId + "/beneficiaries", new HttpEntity<>(beneficiary, getHeaders()), com.wallet.secure.dto.Beneficiary.class);
    }

    // --- Métodos de Archivos (Imágenes) ---

    /**
     * Sube un archivo a la API.
     *
     * @param file Archivo MultipartFile a subir.
     * @return La URL del archivo subido o null en caso de error.
     */
    public String uploadFile(MultipartFile file) {
         try {
            // 1. Obtener headers de autenticación
            HttpHeaders headers = getHeaders(); 
            // 2. Definir tipo Multipart
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            // 3. Enviar a la API
            ResponseEntity<Map> response = restTemplate.postForEntity(API_URL + "/upload", requestEntity, Map.class);
            
            if (response.getBody() != null && response.getBody().containsKey("url")) {
                return (String) response.getBody().get("url");
            }
        } catch (Exception e) {
            logger.error("Error subiendo archivo: " + e.getMessage());
        }
        return null;
    }

    // --- Gestión de Usuario y Contraseña ---

    /**
     * Actualiza la información de un usuario.
     *
     * @param user Objeto User con los datos actualizados.
     */
    public void updateUser(User user) {
        if (user.getId() != null) {
            restTemplate.put(API_URL + "/users/" + user.getId(), new HttpEntity<>(user, getHeaders()));
        }
    }

    /**
     * Elimina un usuario por su ID.
     *
     * @param id ID del usuario a eliminar.
     */
    public void deleteUser(Long id) {
        restTemplate.exchange(API_URL + "/users/" + id, HttpMethod.DELETE, new HttpEntity<>(getHeaders()), Void.class);
    }

    /**
     * Inicia el proceso de recuperación de contraseña.
     *
     * @param email Correo del usuario.
     */
    public void initiatePasswordRecovery(String email) {
        try {
            restTemplate.postForEntity(API_URL + "/auth/forgot-password?email=" + email, null, Void.class);
        } catch (Exception e) {
            logger.error("Error iniciando recuperación: " + e.getMessage());
        }
    }

    /**
     * Restablece la contraseña utilizando el token y la nueva contraseña.
     *
     * @param token Token de recuperación.
     * @param newPassword Nueva contraseña.
     */
    public void resetPassword(String token, String newPassword) {
        try {
            restTemplate.postForEntity(API_URL + "/auth/reset-password", Map.of("token", token, "password", newPassword), Void.class);
        } catch (Exception e) {
             logger.error("Error reseteando contraseña: " + e.getMessage());
        }
    }

    /**
     * Solicita un cambio de rol para un usuario.
     *
     * @param userId ID del usuario.
     * @param newRole Nuevo rol solicitado.
     */
    public void requestRoleChange(Long userId, String newRole) {
        try {
            restTemplate.postForEntity(API_URL + "/users/" + userId + "/role-request?newRole=" + newRole, new HttpEntity<>(getHeaders()), Void.class);
        } catch (Exception e) {
            logger.error("Error solicitando cambio de rol: " + e.getMessage());
        }
    }

    /**
     * Aprueba una solicitud de cambio de rol.
     *
     * @param token Token de la solicitud.
     * @return Null si es exitoso, mensaje de error si falla.
     */
    public String approveRoleChange(String token) {
        try {
            restTemplate.postForEntity(API_URL + "/users/approve-role?token=" + token, new HttpEntity<>(getHeaders()), Void.class);
            return null; // Éxito
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            String serverError = e.getResponseBodyAsString();
            logger.error("Error aprobando rol (Server): " + serverError);
            return serverError.isEmpty() ? e.getMessage() : serverError;
        } catch (Exception e) {
            logger.error("Error aprobando rol: " + e.getMessage());
            return "Error de conexión: " + e.getMessage();
        }
    }

    /**
     * Rechaza una solicitud de cambio de rol.
     *
     * @param token Token de la solicitud.
     * @return Null si es exitoso, mensaje de error si falla.
     */
    public String rejectRoleChange(String token) {
        try {
            restTemplate.postForEntity(API_URL + "/users/reject-role?token=" + token, new HttpEntity<>(getHeaders()), Void.class);
            return null; // Éxito
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            String serverError = e.getResponseBodyAsString();
            logger.error("Error rechazando rol (Server): " + serverError);
            return serverError.isEmpty() ? e.getMessage() : serverError;
        } catch (Exception e) {
            logger.error("Error rechazando rol: " + e.getMessage());
            return "Error de conexión: " + e.getMessage();
        }
    }

    // --- Soporte / Tickets ---

    /**
     * Obtiene todos los tickets de soporte.
     *
     * @return Lista de Tickets.
     */
    public List<Ticket> getAllTickets() {
        try {
            ResponseEntity<List<Ticket>> response = restTemplate.exchange(
                API_URL + "/tickets",
                HttpMethod.GET,
                new HttpEntity<>(getHeaders()),
                new ParameterizedTypeReference<List<Ticket>>() {}
            );
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error obteniendo todos los tickets: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Obtiene los tickets creados por un usuario específico.
     *
     * @param userId ID del usuario.
     * @return Lista de Tickets del usuario.
     */
    public List<Ticket> getTicketsByUser(Long userId) {
        try {
            ResponseEntity<List<Ticket>> response = restTemplate.exchange(
                API_URL + "/tickets/user/" + userId,
                HttpMethod.GET,
                new HttpEntity<>(getHeaders()),
                new ParameterizedTypeReference<List<Ticket>>() {}
            );
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error obteniendo tickets del usuario: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Crea un nuevo ticket de soporte.
     *
     * @param userId ID del usuario que crea el ticket.
     * @param ticket Objeto Ticket.
     */
    public void createTicket(Long userId, Ticket ticket) {
        try {
            restTemplate.postForEntity(API_URL + "/tickets/user/" + userId, new HttpEntity<>(ticket, getHeaders()), Ticket.class);
        } catch (Exception e) {
            logger.error("Error creando ticket: " + e.getMessage());
        }
    }

    /**
     * Cierra un ticket existente.
     *
     * @param ticketId ID del ticket a cerrar.
     */
    public void closeTicket(Long ticketId) {
        try {
            restTemplate.exchange(API_URL + "/tickets/" + ticketId + "/close", HttpMethod.PUT, new HttpEntity<>(getHeaders()), Void.class);
        } catch (Exception e) {
            logger.error("Error cerrando ticket: " + e.getMessage());
        }
    }
}