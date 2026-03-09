package com.fitexa.services.impl;

import com.fitexa.dtos.MachineResponseDto;
import com.fitexa.dtos.DashboardPageResponseDto;
import com.fitexa.entities.MachineEntity;
import com.fitexa.exeptions.ResourceNotFoundException;
import com.fitexa.repository.MachineRepository;
import com.fitexa.services.MachinePhotoStorageService;
import com.fitexa.services.MachineService;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MachineServiceImpl implements MachineService {

    private static final int MAX_PAGE_SIZE = 100;

    private final MachineRepository machineRepository;
    private final MachinePhotoStorageService machinePhotoStorageService;

    @Override
    @Transactional(readOnly = true)
    public List<MachineResponseDto> findAll() {
        return machineRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardPageResponseDto<MachineResponseDto> findPage(String name, boolean onlyInUse, int page, int size) {
        Page<MachineResponseDto> result = machineRepository.findForTable(
            clean(name),
            onlyInUse,
            PageRequest.of(sanitizePage(page), sanitizeSize(size), Sort.by(Sort.Direction.ASC, "name"))
        ).map(this::toDto);

        return toPageResponse(result);
    }

    @Override
    @Transactional(readOnly = true)
    public MachineResponseDto findById(Long id) {
        return toDto(findEntity(id));
    }

    @Override
    @Transactional
    public MachineResponseDto create(String name, MultipartFile photo) {
        MachineEntity entity = MachineEntity.builder()
            .name(cleanRequired(name))
            .photoUrl(storePhoto(photo))
            .build();
        return toDto(machineRepository.save(entity));
    }

    @Override
    @Transactional
    public MachineResponseDto update(Long id, String name, MultipartFile photo) {
        MachineEntity entity = findEntity(id);
        entity.setName(cleanRequired(name));
        if (photo != null && !photo.isEmpty()) {
            replaceStoredPhoto(entity, photo);
        }
        return toDto(machineRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        MachineEntity entity = findEntity(id);
        deleteStoredPhoto(entity.getPhotoUrl());
        machineRepository.delete(entity);
    }

    private MachineEntity findEntity(Long id) {
        return machineRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Maquina no encontrada: " + id));
    }

    private MachineResponseDto toDto(MachineEntity entity) {
        return MachineResponseDto.builder()
            .id(entity.getId())
            .name(entity.getName())
            .photoUrl(entity.getPhotoUrl())
            .build();
    }

    private <T> DashboardPageResponseDto<T> toPageResponse(Page<T> page) {
        return DashboardPageResponseDto.<T>builder()
            .content(page.getContent())
            .page(page.getNumber())
            .size(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .build();
    }

    private String clean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String cleanRequired(String value) {
        String cleaned = clean(value);
        if (cleaned == null) {
            throw new IllegalArgumentException("El nombre de la maquina es obligatorio.");
        }
        return cleaned;
    }

    private int sanitizePage(int page) {
        return Math.max(0, page);
    }

    private int sanitizeSize(int size) {
        if (size <= 0) {
            return 8;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }

    private String storePhoto(MultipartFile photo) {
        try {
            return machinePhotoStorageService.store(photo);
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo guardar la imagen de la maquina.", ex);
        }
    }

    private void replaceStoredPhoto(MachineEntity entity, MultipartFile photo) {
        deleteStoredPhoto(entity.getPhotoUrl());
        entity.setPhotoUrl(storePhoto(photo));
    }

    private void deleteStoredPhoto(String photoUrl) {
        try {
            machinePhotoStorageService.deleteByPhotoUrl(photoUrl);
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo eliminar la imagen de la maquina.", ex);
        }
    }
}
