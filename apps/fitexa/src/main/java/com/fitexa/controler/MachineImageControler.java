package com.fitexa.controler;

import com.fitexa.services.MachinePhotoStorageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/machines/images")
@RequiredArgsConstructor
@Tag(name = "Imagenes Publicas", description = "Acceso publico a imagenes de maquinas.")
public class MachineImageControler {

    private final MachinePhotoStorageService machinePhotoStorageService;

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> getMachineImage(@PathVariable String filename) throws IOException {
        Resource resource = machinePhotoStorageService.loadAsResource(filename);
        String contentType = Files.probeContentType(Path.of(resource.getFile().getAbsolutePath()));

        return ResponseEntity.ok()
            .contentType(contentType == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
            .body(resource);
    }
}
