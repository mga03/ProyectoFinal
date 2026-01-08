package com.wallet.secure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ModelAndView handleMaxSizeException(MaxUploadSizeExceededException exc) {
        logger.error("Intento de subida de archivo demasiado grande: " + exc.getMessage());
        ModelAndView modelAndView = new ModelAndView("error");
        modelAndView.addObject("message", "El archivo es demasiado grande. Por favor, sube un archivo más pequeño.");
        return modelAndView;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(Exception exc) {
        logger.error("Error no controlado: ", exc);
        ModelAndView modelAndView = new ModelAndView("error");
        modelAndView.addObject("message", "Ha ocurrido un error inesperado: " + exc.getMessage());
        return modelAndView;
    }
}
