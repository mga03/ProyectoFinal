package com.wallet.secure.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.io.UnsupportedEncodingException;

/**
 * Servicio encargado del envío de correos electrónicos.
 * Maneja notificaciones de registro, recuperación de contraseña y gestión de roles.
 */
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // --- CONFIGURACIÓN DE URL DEL FRONTEND ---
    // Esta variable viene de application.properties (app.frontend.url)
    @Value("${app.frontend.url:http://localhost:8888}")
    private String FRONTEND_URL;

    /**
     * Envía un correo de verificación de cuenta al usuario recién registrado.
     *
     * @param recipientEmail Dirección de correo del destinatario.
     * @param code Código de verificación único.
     * @throws MessagingException Error en la creación del mensaje.
     * @throws UnsupportedEncodingException Error con la codificación de caracteres.
     */
    public void sendVerificationEmail(String recipientEmail, String code) 
            throws MessagingException, UnsupportedEncodingException {
        
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom("guarinosmanuel07@gmail.com", "Wallet Secure");
        helper.setTo(recipientEmail);
        helper.setSubject("Bienvenido a Wallet Secure - Activa tu cuenta");
        
        String content = "<html><body>"
                + "<div style='font-family: Arial, sans-serif; padding: 20px; border: 1px solid #ddd; max-width: 600px;'>"
                + "<h2 style='color: #0d6efd;'>¡Bienvenido a bordo!</h2>"
                + "<p>Gracias por registrarte en Wallet Secure. Para activar tu cuenta, por favor haz clic en el siguiente botón:</p>"
                + "<a href='" + FRONTEND_URL + "/verify?code=" + code + "' "
                + "style='background-color: #198754; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block; margin: 10px 0;'>"
                + "ACTIVAR CUENTA AHORA</a>"
                + "<p style='margin-top: 20px; font-size: 12px; color: #666;'>Si no te has registrado, ignora este mensaje.</p>"
                + "</div></body></html>";
        
        helper.setText(content, true);
        mailSender.send(message);
        System.out.println("INFO: Correo enviado a: " + recipientEmail);
    }
    
    /**
     * Envía un correo con un enlace para restablecer la contraseña.
     *
     * @param recipientEmail Dirección de correo del destinatario.
     * @param token Token único de recuperación.
     * @throws MessagingException Error en la creación del mensaje.
     * @throws UnsupportedEncodingException Error con la codificación de caracteres.
     */
    public void sendPasswordResetEmail(String recipientEmail, String token)
            throws MessagingException, UnsupportedEncodingException {
            
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom("guarinosmanuel07@gmail.com", "Wallet Secure");
        helper.setTo(recipientEmail);
        helper.setSubject("Recuperación de Contraseña - Wallet Secure");
        
        String content = "<html><body>"
                + "<div style='font-family: Arial, sans-serif; padding: 20px; border: 1px solid #ddd; max-width: 600px;'>"
                + "<h2 style='color: #dc3545;'>Restablecer Contraseña</h2>"
                + "<p>Hemos recibido una solicitud para cambiar tu contraseña.</p>"
                + "<p>Haz clic en el siguiente botón para crear una nueva:</p>"
                + "<a href='" + FRONTEND_URL + "/reset-password?token=" + token + "' "
                + "style='background-color: #dc3545; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block; margin: 10px 0;'>"
                + "CAMBIAR CONTRASEÑA</a>"
                + "<p style='margin-top: 20px; font-size: 12px; color: #666;'>Si no has solicitado esto, ignora este mensaje.</p>"
                + "</div></body></html>";
                
        helper.setText(content, true);
        mailSender.send(message);
        System.out.println("INFO: Correo de recuperación enviado a: " + recipientEmail);
    }

    /**
     * Envía una solicitud de cambio de rol al administrador.
     *
     * @param userEmail Correo del usuario que solicita el cambio.
     * @param desiredRole Rol solicitado.
     * @param token Token de aprobación.
     * @throws MessagingException Error en el envío del correo.
     */
    public void sendAdminRoleRequest(String userEmail, String desiredRole, String token) throws MessagingException {
        System.out.println("INFO: Intentando enviar solicitud de admin. De: " + userEmail);
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom("guarinosmanuel07@gmail.com");
        helper.setTo("guarinosmanuel07@gmail.com"); 
        helper.setSubject("Solicitud de Cambio de Rol: " + userEmail);

        // La aprobación de roles es lógica interna, usualmente el Admin se loguea para aprobar.
        // Si se necesitan enlaces externos, deben apuntar al Frontend que llama a la API.
        String approveLink = FRONTEND_URL + "/role-approval/approve?token=" + token;
        String rejectLink = FRONTEND_URL + "/role-approval/reject?token=" + token;
        
        String content = "<html><body>"
                + "<h3>Solicitud de Cambio de Rol</h3>"
                + "<p>El usuario <b>" + userEmail + "</b> solicita ser: <b style='color:blue'>" + desiredRole + "</b></p>"
                + "<p>¿Qué deseas hacer?</p>"
                + "<a href='" + approveLink + "' style='padding:10px; background:green; color:white; text-decoration:none; border-radius:5px;'>ACEPTAR</a> "
                + "<a href='" + rejectLink + "' style='padding:10px; background:red; color:white; text-decoration:none; border-radius:5px; margin-left:10px;'>RECHAZAR</a>"
                + "</body></html>";

        helper.setText(content, true);
        mailSender.send(message);
    }

    /**
     * Notifica al usuario sobre el estado de su solicitud de cambio de rol.
     *
     * @param userEmail Correo del usuario.
     * @param status Estado de la solicitud (APROBADO/DENEGADO).
     * @param roleName Nombre del rol asignado.
     * @throws MessagingException Error en el envío del correo.
     */
    public void sendRoleStatusEmail(String userEmail, String status, String roleName) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom("guarinosmanuel07@gmail.com");
        helper.setTo(userEmail);
        helper.setSubject("Respuesta a tu Solicitud de Rol");

        String content = "<html><body>"
                + "<h3>Notificación de Cambio de Rol</h3>"
                + (status.equals("APROBADO") 
                    ? "<p>Hola,</p><p>Te informamos que <b>tu rol ha sido cambiado exitosamente</b>.</p>"
                    + "<p>Nuevo Rol: <b style='color:green'>" + roleName + "</b></p>"
                    + "<p>Por favor, Cierra Sesión y vuelve a entrar para ver las nuevas opciones.</p>" 
                    : "<p>Hola,</p><p>Tu solicitud de cambio de rol ha sido <span style='color:red'>DENEGADA</span>.</p>")
                + "</body></html>";

        helper.setText(content, true);
        mailSender.send(message);
    }
}