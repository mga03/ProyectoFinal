package com.wallet.secure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal de arranque para la aplicación frontend (wallet-web).
 * Inicia el servidor web (Tomcat embebido) y la configuración de Spring MVC.
 */
@SpringBootApplication
public class SecureApplication {

    private static final Logger logger = LoggerFactory.getLogger(SecureApplication.class);

    /**
     * Método main que arranca la aplicación.
     *
     * @param args Argumentos de consola.
     */
    public static void main(String[] args) {
        SpringApplication.run(SecureApplication.class, args);
        logger.info("Aplicacion Wallet Web iniciada correctamente.");
    }
}
