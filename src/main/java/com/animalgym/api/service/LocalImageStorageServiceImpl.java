package com.animalgym.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "app.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalImageStorageServiceImpl implements ImageStorageService {

    private final Path uploadDir;
    private final String baseUrl;

    public LocalImageStorageServiceImpl(
            @Value("${app.upload-dir:uploads}") String uploadDirPath,
            @Value("${app.base-url:http://localhost:8080}") String baseUrl
    ) {
        this.baseUrl = baseUrl;
        this.uploadDir = Paths.get(uploadDirPath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize upload directory: " + this.uploadDir, e);
        }
    }

    @Override
    public String uploadImage(MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String uniqueFilename = UUID.randomUUID().toString() + extension;
            Path targetPath = this.uploadDir.resolve(uniqueFilename);

            // REPLACE_EXISTING evita FileAlreadyExistsException en caso de colisión de UUID
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Construimos la URL a partir del baseUrl inyectado desde properties,
            // sin depender del contexto HTTP del request (lo que causaba el 500).
            return baseUrl + "/uploads/" + uniqueFilename;

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return;
        try {
            // Extraemos solo el nombre del archivo de la URL
            String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            Path filePath = this.uploadDir.resolve(filename);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Loguear pero no explotar; el borrado de imagen no debe romper el flujo principal
            System.err.println("Warning: could not delete image file: " + e.getMessage());
        }
    }
}
