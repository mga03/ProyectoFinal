package com.wallet.secure.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuración MVC de la aplicación web.
 * Define manejadores de recursos estáticos e interceptores.
 */
@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Autowired
    private RoleSyncInterceptor roleSyncInterceptor;

    /**
     * Configura el manejo de recursos estáticos, incluyendo la carpeta de subidas.
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

    /**
     * Registra interceptores personalizados.
     *
     * @param registry Registro de interceptores.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Ejecutar sincronización de roles en todas las peticiones excepto recursos estáticos
        registry.addInterceptor(roleSyncInterceptor)
                .excludePathPatterns("/css/**", "/js/**", "/images/**", "/uploads/**", "/webjars/**", "/error");
    }
}
