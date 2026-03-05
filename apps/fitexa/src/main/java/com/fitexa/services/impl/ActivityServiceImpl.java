package com.fitexa.services.impl;

import com.fitexa.dtos.ActivityRequestDto;
import com.fitexa.dtos.ActivityResponseDto;
import com.fitexa.entities.ActivityEntity;
import com.fitexa.exeptions.ResourceNotFoundException;
import com.fitexa.repository.ActivityRepository;
import com.fitexa.services.ActivityService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private final ActivityRepository activityRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ActivityResponseDto> findAll() {
        return activityRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityResponseDto findById(Long id) {
        return toDto(findEntity(id));
    }

    @Override
    @Transactional
    public ActivityResponseDto create(ActivityRequestDto request) {
        ActivityEntity entity = ActivityEntity.builder()
            .name(request.getName())
            .description(request.getDescription())
            .build();
        return toDto(activityRepository.save(entity));
    }

    @Override
    @Transactional
    public ActivityResponseDto update(Long id, ActivityRequestDto request) {
        ActivityEntity entity = findEntity(id);
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        return toDto(activityRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        activityRepository.delete(findEntity(id));
    }

    private ActivityEntity findEntity(Long id) {
        return activityRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada: " + id));
    }

    private ActivityResponseDto toDto(ActivityEntity entity) {
        return ActivityResponseDto.builder()
            .id(entity.getId())
            .name(entity.getName())
            .description(entity.getDescription())
            .build();
    }
}
