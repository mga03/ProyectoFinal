package com.wallet.secure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Manejador global de excepciones para la aplicación web.
 * Intercepta errores y presenta mensajes amigables al usuario.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Gestiona el error cuando se carga un archivo que excede el tamaño permitido.
     *
     * @param exc Excepción de tamaño máximo excedido.
     * @return Vista de error con mensaje explicativo.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ModelAndView handleMaxSizeException(MaxUploadSizeExceededException exc) {
        logger.error("Intento de subida de archivo demasiado grande: " + exc.getMessage());
        ModelAndView modelAndView = new ModelAndView("error");
        modelAndView.addObject("message", "El archivo es demasiado grande. Por favor, sube un archivo más pequeño.");
        return modelAndView;
    }

    /**
     * Gestiona errores 404 para recursos estáticos.
     *
     * @param ex Excepción de recurso no encontrado.
     * @return Respuesta 404 (Not Found).
     */
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public org.springframework.http.ResponseEntity<Void> handleResourceNotFound(org.springframework.web.servlet.resource.NoResourceFoundException ex) {
        return org.springframework.http.ResponseEntity.notFound().build();
    }

    /**
     * Manejador genérico para cualquier otra excepción no capturada.
     *
     * @param exc La excepción lanzada.
     * @return Vista de error con detalles (si aplica).
     * @throws Exception Si es una excepción de seguridad, se propaga.
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
