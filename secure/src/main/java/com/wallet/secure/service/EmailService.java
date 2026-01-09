package com.wallet.secure.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

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
                + "<a href='http://localhost:8081/verify?code=" + code + "' "
                + "style='background-color: #198754; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block; margin: 10px 0;'>"
                + "ACTIVAR CUENTA AHORA</a>"
                + "<p style='margin-top: 20px; font-size: 12px; color: #666;'>Si no te has registrado, ignora este mensaje.</p>"
                + "</div></body></html>";
        
        helper.setText(content, true);
        mailSender.send(message);
        System.out.println("✅ Correo enviado a: " + recipientEmail);
    }
    
    public void sendPasswordResetEmail(String recipientEmail, String token)
            throws MessagingException, UnsupportedEncodingException {
            
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        // IMPORTANTE: Usar el mismo correo autenticado que en application.properties
        helper.setFrom("guarinosmanuel07@gmail.com", "Wallet Secure");
        helper.setTo(recipientEmail);
        helper.setSubject("Recuperación de Contraseña - Wallet Secure");
        
        String content = "<html><body>"
                + "<div style='font-family: Arial, sans-serif; padding: 20px; border: 1px solid #ddd; max-width: 600px;'>"
                + "<h2 style='color: #dc3545;'>Restablecer Contraseña</h2>"
                + "<p>Hemos recibido una solicitud para cambiar tu contraseña.</p>"
                + "<p>Haz clic en el siguiente botón para crear una nueva:</p>"
                + "<a href='http://localhost:8081/reset-password?token=" + token + "' "
                + "style='background-color: #dc3545; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block; margin: 10px 0;'>"
                + "CAMBIAR CONTRASEÑA</a>"
                + "<p style='margin-top: 20px; font-size: 12px; color: #666;'>Si no has solicitado esto, ignora este mensaje.</p>"
                + "</div></body></html>";
                
        helper.setText(content, true);
        mailSender.send(message);
        System.out.println("✅ Correo de recuperación enviado a: " + recipientEmail);
    }
}
