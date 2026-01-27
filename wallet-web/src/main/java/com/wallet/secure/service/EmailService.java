package com.wallet.secure.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

/**
 * Servicio encargado del envío de correos electrónicos desde el cliente web.
 * Se utiliza para notificaciones de registro, recuperación de contraseña y gestión de roles.
 */
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // --- CONFIGURACIÓN DE TU DOMINIO NGROK ---
    private final String BASE_URL = "https://nontraditionalistic-unactinic-jung.ngrok-free.dev";

    /**
     * Envía un correo de verificación de cuenta al usuario.
     *
     * @param recipientEmail Correo del destinatario.
     * @param code Código de verificación.
     * @throws MessagingException Error general de mensajería.
     * @throws UnsupportedEncodingException Error de codificación de caracteres.
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
                + "<a href='" + BASE_URL + "/verify?code=" + code + "' "
                + "style='background-color: #198754; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block; margin: 10px 0;'>"
                + "ACTIVAR CUENTA AHORA</a>"
                + "<p style='margin-top: 20px; font-size: 12px; color: #666;'>Si no te has registrado, ignora este mensaje.</p>"
                + "</div></body></html>";
        
        helper.setText(content, true);
        mailSender.send(message);
        System.out.println("INFO: Correo enviado a: " + recipientEmail);
    }
    
    /**
     * Envía un correo para restablecer la contraseña.
     *
     * @param recipientEmail Correo del destinatario.
     * @param token Token de recuperación.
     * @throws MessagingException Error general de mensajería.
     * @throws UnsupportedEncodingException Error de codificación.
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
                + "<a href='" + BASE_URL + "/reset-password?token=" + token + "' "
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
     * @param userEmail Correo del usuario que solicita.
     * @param desiredRole Rol deseado.
     * @param token Token de solicitud.
     * @throws MessagingException Error enviando el correo.
     */
    public void sendAdminRoleRequest(String userEmail, String desiredRole, String token) throws MessagingException {
        System.out.println("INFO: Intentando enviar solicitud de admin. De: " + userEmail);
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom("guarinosmanuel07@gmail.com");
        helper.setTo("guarinosmanuel07@gmail.com"); 
        helper.setSubject("Solicitud de Cambio de Rol: " + userEmail);

        String approveLink = BASE_URL + "/role-approval/approve?token=" + token;
        String rejectLink = BASE_URL + "/role-approval/reject?token=" + token;
        
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
     * Notifica al usuario la respuesta a su solicitud de rol.
     *
     * @param userEmail Correo del usuario.
     * @param status Estado de la solicitud.
     * @param roleName Nombre del rol.
     * @throws MessagingException Error enviando el correo.
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