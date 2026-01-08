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
        MimeMessageHelper helper = new MimeMessageHelper(message);
        
        helper.setFrom("no-reply@walletsecure.com", "Wallet Secure Support");
        helper.setTo(recipientEmail);
        helper.setSubject("Verifica tu cuenta - Wallet Secure");
        
        String content = "<div style=\"font-family: Arial, sans-serif; padding: 20px;\">"
                + "<h2 style=\"color: #333;\">Bienvenido a Wallet Secure</h2>"
                + "<p>Gracias por registrarte. Por favor, verifica tu cuenta haciendo clic en el enlace de abajo:</p>"
                + "<p><a href=\"http://localhost:8081/verify?code=" + code + "\" "
                + "style=\"background-color: #007bff; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;\">"
                + "Verificar Cuenta</a></p>"
                + "<p>Si no te has registrado, ignora este correo.</p>"
                + "</div>";
        
        helper.setText(content, true);
        mailSender.send(message);
    }
    
    public void sendPasswordResetEmail(String recipientEmail, String token)
            throws MessagingException, UnsupportedEncodingException {
            
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        
        helper.setFrom("no-reply@walletsecure.com", "Wallet Secure Support");
        helper.setTo(recipientEmail);
        helper.setSubject("Recuperación de Contraseña");
        
        String content = "<div style=\"font-family: Arial, sans-serif; padding: 20px;\">"
                + "<h2 style=\"color: #333;\">Restablecer Contraseña</h2>"
                + "<p>Hemos recibido una solicitud para restablecer tu contraseña.</p>"
                + "<p><a href=\"http://localhost:8081/reset-password?token=" + token + "\" "
                + "style=\"background-color: #dc3545; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;\">"
                + "Cambiar Contraseña</a></p>"
                + "<p>Si no solicitaste esto, ignora este mensaje.</p>"
                + "</div>";
                
        helper.setText(content, true);
        mailSender.send(message);
    }
}
