package com.fitexa.services;

import java.io.IOException;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface MachinePhotoStorageService {
    String store(MultipartFile file) throws IOException;
    void deleteByPhotoUrl(String photoUrl) throws IOException;
    Resource loadAsResource(String filename) throws IOException;
    boolean isStoredPhotoUrl(String photoUrl);
}
