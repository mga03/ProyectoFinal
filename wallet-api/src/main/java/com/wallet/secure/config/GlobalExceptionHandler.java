package com.wallet.secure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Manejador global de excepciones para la aplicación.
 * Captura errores no controlados y muestra páginas de error amigables o respuestas adecuadas.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maneja la excepción cuando se excede el tamaño máximo de subida de archivos.
     *
     * @param exc La excepción capturada.
     * @return Vista de error con un mensaje explicativo.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ModelAndView handleMaxSizeException(MaxUploadSizeExceededException exc) {
        logger.error("Intento de subida de archivo demasiado grande: " + exc.getMessage());
        ModelAndView modelAndView = new ModelAndView("error");
        modelAndView.addObject("message", "El archivo es demasiado grande. Por favor, sube un archivo más pequeño.");
        return modelAndView;
    }

    /**
     * Maneja excepciones de recursos estáticos no encontrados (404).
     *
     * @param ex La excepción capturada.
     * @return Respuesta HTTP 404 Not Found.
     */
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public org.springframework.http.ResponseEntity<Void> handleResourceNotFound(org.springframework.web.servlet.resource.NoResourceFoundException ex) {
        return org.springframework.http.ResponseEntity.notFound().build();
    }

    /**
     * Maneja cualquier excepción no controlada explícitamente.
     *
     * @param exc La excepción capturada.
     * @return Vista de error genérica.
     * @throws Exception Si la excepción es de seguridad, se relanza.
     */
    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(Exception exc) throws Exception {
        // Validación DEBUG: Imprimir la excepción real
        System.out.println("ERROR RUNTIME: " + exc.getClass().getName());
        exc.printStackTrace();

        // Si es un error de seguridad (AccessDenied), lo relanzamos para que Spring Security lo maneje
        if (exc instanceof org.springframework.security.access.AccessDeniedException) {
            throw exc;
        }

        logger.error("Error no controlado: ", exc);
        ModelAndView modelAndView = new ModelAndView("error");
        modelAndView.addObject("message", "Ha ocurrido un error inesperado: " + exc.getMessage());
        return modelAndView;
    }
}
