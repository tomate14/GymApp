package com.fitexa.services.impl;

import com.fitexa.services.MachinePhotoStorageService;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MachinePhotoStorageServiceImpl implements MachinePhotoStorageService {
    private static final String PUBLIC_PREFIX = "/api/v1/public/machines/images/";

    @Value("${fitexa.storage.machine-images-dir:/app/data/machine-images}")
    private String machineImagesDir;

    @Override
    public String store(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("La imagen de la maquina es obligatoria.");
        }

        Files.createDirectories(resolveStorageDir());

        String filename = UUID.randomUUID() + extractExtension(file.getOriginalFilename());
        Path target = resolveStorageDir().resolve(filename).normalize();

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        }

        return PUBLIC_PREFIX + filename;
    }

    @Override
    public void deleteByPhotoUrl(String photoUrl) throws IOException {
        if (!isStoredPhotoUrl(photoUrl)) {
            return;
        }
        String filename = photoUrl.substring(PUBLIC_PREFIX.length());
        Files.deleteIfExists(resolveStorageDir().resolve(filename).normalize());
    }

    @Override
    public Resource loadAsResource(String filename) throws IOException {
        Path file = resolveStorageDir().resolve(filename).normalize();
        Resource resource = new UrlResource(file.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            throw new IOException("No se pudo leer la imagen de maquina: " + filename);
        }
        return resource;
    }

    @Override
    public boolean isStoredPhotoUrl(String photoUrl) {
        return photoUrl != null && photoUrl.startsWith(PUBLIC_PREFIX);
    }

    private Path resolveStorageDir() {
        return Paths.get(machineImagesDir).toAbsolutePath().normalize();
    }

    private String extractExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return ".jpg";
        }
        String extension = originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase(Locale.ROOT);
        String normalized = Normalizer.normalize(extension, Normalizer.Form.NFD).replaceAll("[^a-zA-Z0-9.]", "");
        return normalized.isBlank() ? ".jpg" : normalized;
    }
}
