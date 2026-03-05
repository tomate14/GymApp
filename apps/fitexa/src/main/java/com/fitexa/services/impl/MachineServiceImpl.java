package com.fitexa.services.impl;

import com.fitexa.dtos.MachineRequestDto;
import com.fitexa.dtos.MachineResponseDto;
import com.fitexa.entities.MachineEntity;
import com.fitexa.exeptions.ResourceNotFoundException;
import com.fitexa.repository.MachineRepository;
import com.fitexa.services.MachineService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MachineServiceImpl implements MachineService {

    private final MachineRepository machineRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MachineResponseDto> findAll() {
        return machineRepository.findAll().stream().map(this::toDto).toList();
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
}
