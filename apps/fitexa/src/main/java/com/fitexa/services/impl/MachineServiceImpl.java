package com.fitexa.services.impl;

import com.fitexa.dtos.MachineRequestDto;
import com.fitexa.dtos.MachineResponseDto;
import com.fitexa.dtos.DashboardPageResponseDto;
import com.fitexa.entities.MachineEntity;
import com.fitexa.exeptions.ResourceNotFoundException;
import com.fitexa.repository.MachineRepository;
import com.fitexa.services.MachineService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MachineServiceImpl implements MachineService {

    private static final int MAX_PAGE_SIZE = 100;

    private final MachineRepository machineRepository;

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
    public MachineResponseDto create(MachineRequestDto request) {
        MachineEntity entity = MachineEntity.builder()
            .name(request.getName())
            .photoUrl(request.getPhotoUrl())
            .build();
        return toDto(machineRepository.save(entity));
    }

    @Override
    @Transactional
    public MachineResponseDto update(Long id, MachineRequestDto request) {
        MachineEntity entity = findEntity(id);
        entity.setName(request.getName());
        entity.setPhotoUrl(request.getPhotoUrl());
        return toDto(machineRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        machineRepository.delete(findEntity(id));
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

    private int sanitizePage(int page) {
        return Math.max(0, page);
    }

    private int sanitizeSize(int size) {
        if (size <= 0) {
            return 8;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }
}
