package com.wallet.secure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuración MVC de la aplicación.
 * <p>
 * Define los manejadores de recursos estáticos, específicamente para
 * servir los archivos subidos al directorio 'uploads'.
 * </p>
 */
@Configuration
public class MvcConfig implements WebMvcConfigurer {

    /**
     * Configura el manejador de recursos para servir archivos subidos.
     * <p>
     * Mapea la URL `/uploads/**` al directorio físico `./uploads` del servidor.
     * Crea el directorio si no existe.
     * </p>
     *
     * @param registry Registro de manejadores de recursos.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadDir = Paths.get("./uploads");
        String uploadPath = uploadDir.toFile().getAbsolutePath();

        try {
            if (!java.nio.file.Files.exists(uploadDir)) {
                java.nio.file.Files.createDirectories(uploadDir);
            }
        } catch (java.io.IOException e) {
            throw new RuntimeException("Could not verify/create uploads directory", e);
        }

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:/" + uploadPath + "/");
    }
}
