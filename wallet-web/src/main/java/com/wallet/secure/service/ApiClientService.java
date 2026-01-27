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

@Service
public class ApiClientService {

    private final RestTemplate restTemplate = new RestTemplate();
    
    @org.springframework.beans.factory.annotation.Value("${app.api.url:http://localhost:8081/api}")
    private String API_URL;

    private static final Logger logger = LoggerFactory.getLogger(ApiClientService.class);

    /**
     * Genera los headers incluyendo el Token de "Identidad" (X-Auth-User).
     * Es vital para que la API sepa quién está haciendo la petición.
     */
    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            
            // Lógica robusta para extraer el email sin importar el tipo de objeto
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

    // --- Auth & User ---

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
    // --- Nuevo método para el Panel Admin ---
    public List<User> getAllUsers() {
        try {
            ResponseEntity<List<User>> response = restTemplate.exchange(
                API_URL + "/users", // Asumimos que tu UserRestController tiene este endpoint mapeado
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

    public void registerUser(User user) {
        restTemplate.postForEntity(API_URL + "/auth/register", user, Map.class);
    }

    public boolean verifyUser(String code) {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(API_URL + "/auth/verify?code={code}", Map.class, code);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    public User getUserByEmail(String email) {
        try {
            // Truco: Enviamos el propio email en el header para "auto-autorizarnos" 
            // y que la API nos permita descargar los datos del usuario.
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

    // --- Insurance ---

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
            return List.of(); // Retornar lista vacía si falla para no romper la web
        }
    }

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

    public void saveInsurance(Insurance insurance) {
        if (insurance.getId() == null) {
            restTemplate.postForEntity(API_URL + "/insurances", new HttpEntity<>(insurance, getHeaders()), Insurance.class);
        } else {
            restTemplate.exchange(API_URL + "/insurances/" + insurance.getId(), HttpMethod.PUT, new HttpEntity<>(insurance, getHeaders()), Insurance.class);
        }
    }

    public void deleteInsurance(Long id) {
        restTemplate.exchange(API_URL + "/insurances/" + id, HttpMethod.DELETE, new HttpEntity<>(getHeaders()), Void.class);
    }
    
    public void saveClaim(com.wallet.secure.dto.Claim claim, Long insuranceId) {
        // Asegúrate de que tu API tenga este endpoint mapeado
        restTemplate.postForEntity(API_URL + "/claims/insurance/" + insuranceId, new HttpEntity<>(claim, getHeaders()), com.wallet.secure.dto.Claim.class);
    }
    
    public void saveBeneficiary(com.wallet.secure.dto.Beneficiary beneficiary, Long insuranceId) {
        restTemplate.postForEntity(API_URL + "/insurances/" + insuranceId + "/beneficiaries", new HttpEntity<>(beneficiary, getHeaders()), com.wallet.secure.dto.Beneficiary.class);
    }

    // --- MÉTODOS DE ARCHIVOS (Imágenes) ---

    public String uploadFile(MultipartFile file) {
         try {
            // 1. OBTENER HEADERS DE AUTENTICACIÓN (CRÍTICO)
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

    public void updateUser(User user) {
        if (user.getId() != null) {
            restTemplate.put(API_URL + "/users/" + user.getId(), new HttpEntity<>(user, getHeaders()));
        }
    }

    public void deleteUser(Long id) {
        restTemplate.exchange(API_URL + "/users/" + id, HttpMethod.DELETE, new HttpEntity<>(getHeaders()), Void.class);
    }

    public void initiatePasswordRecovery(String email) {
        try {
            restTemplate.postForEntity(API_URL + "/auth/forgot-password?email=" + email, null, Void.class);
        } catch (Exception e) {
            logger.error("Error iniciando recuperación: " + e.getMessage());
        }
    }

    public void resetPassword(String token, String newPassword) {
        try {
            restTemplate.postForEntity(API_URL + "/auth/reset-password", Map.of("token", token, "password", newPassword), Void.class);
        } catch (Exception e) {
             logger.error("Error reseteando contraseña: " + e.getMessage());
        }
    }
    public void requestRoleChange(Long userId, String newRole) {
        try {
            restTemplate.postForEntity(API_URL + "/users/" + userId + "/role-request?newRole=" + newRole, new HttpEntity<>(getHeaders()), Void.class);
        } catch (Exception e) {
            logger.error("Error solicitando cambio de rol: " + e.getMessage());
        }
    }

    public boolean approveRoleChange(String token) {
        try {
            restTemplate.postForEntity(API_URL + "/users/approve-role?token=" + token, new HttpEntity<>(getHeaders()), Void.class);
            return true;
        } catch (Exception e) {
            logger.error("Error aprobando rol: " + e.getMessage());
            return false;
        }
    }

    public boolean rejectRoleChange(String token) {
        try {
            restTemplate.postForEntity(API_URL + "/users/reject-role?token=" + token, new HttpEntity<>(getHeaders()), Void.class);
            return true;
        } catch (Exception e) {
            logger.error("Error rechazando rol: " + e.getMessage());
            return false;
        }
    }

    // --- Soporte / Tickets ---

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

    public void createTicket(Long userId, Ticket ticket) {
        try {
            restTemplate.postForEntity(API_URL + "/tickets/user/" + userId, new HttpEntity<>(ticket, getHeaders()), Ticket.class);
        } catch (Exception e) {
            logger.error("Error creando ticket: " + e.getMessage());
        }
    }

    public void closeTicket(Long ticketId) {
        try {
            restTemplate.exchange(API_URL + "/tickets/" + ticketId + "/close", HttpMethod.PUT, new HttpEntity<>(getHeaders()), Void.class);
        } catch (Exception e) {
            logger.error("Error cerrando ticket: " + e.getMessage());
        }
    }
}