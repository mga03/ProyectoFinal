package com.wallet.secure.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Controlador REST para la gestión de subida de archivos.
 * <p>
 * Gestiona el almacenamiento de archivos (imágenes, documentos) en el servidor.
 * </p>
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class FileController {

    private static String UPLOAD_DIR = "uploads/";

    /**
     * Sube un archivo al servidor.
     *
     * @param file El archivo binario a subir (MultipartFile).
     * @return ResponseEntity con la URL de acceso al archivo o mensaje de error.
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "File is empty"));
        }

        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);

            String fileUrl = "/uploads/" + fileName;
            return ResponseEntity.ok(Map.of("url", fileUrl));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("message", "Could not upload file: " + e.getMessage()));
        }
        
    }
   
}
