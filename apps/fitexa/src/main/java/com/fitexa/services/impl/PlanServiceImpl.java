package com.fitexa.services.impl;

import com.fitexa.dtos.PlanRequestDto;
import com.fitexa.dtos.PlanResponseDto;
import com.fitexa.entities.PlanEntity;
import com.fitexa.exeptions.ResourceNotFoundException;
import com.fitexa.repository.PlanRepository;
import com.fitexa.services.PlanService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {

    private final PlanRepository planRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PlanResponseDto> findAll() {
        return planRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanResponseDto> findActive() {
        return planRepository.findAll().stream()
            .filter(PlanEntity::getActive)
            .map(this::toDto)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PlanResponseDto findById(Long id) {
        return toDto(findEntity(id));
    }

    @Override
    @Transactional
    public PlanResponseDto create(PlanRequestDto request) {
        PlanEntity entity = PlanEntity.builder()
            .name(request.getName())
            .price(request.getPrice())
            .maxDaysPerWeek(request.getMaxDaysPerWeek())
            .active(request.getActive())
            .build();
        return toDto(planRepository.save(entity));
    }

    @Override
    @Transactional
    public PlanResponseDto update(Long id, PlanRequestDto request) {
        PlanEntity entity = findEntity(id);
        entity.setName(request.getName());
        entity.setPrice(request.getPrice());
        entity.setMaxDaysPerWeek(request.getMaxDaysPerWeek());
        entity.setActive(request.getActive());
        return toDto(planRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        planRepository.delete(findEntity(id));
    }

    private PlanEntity findEntity(Long id) {
        return planRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Plan no encontrado: " + id));
    }

    private PlanResponseDto toDto(PlanEntity entity) {
        return PlanResponseDto.builder()
            .id(entity.getId())
            .name(entity.getName())
            .price(entity.getPrice())
            .maxDaysPerWeek(entity.getMaxDaysPerWeek())
            .active(entity.getActive())
            .build();
    }
}
