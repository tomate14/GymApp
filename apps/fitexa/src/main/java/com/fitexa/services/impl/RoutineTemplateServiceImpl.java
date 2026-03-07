package com.fitexa.services.impl;

import com.fitexa.dtos.RoutineTemplateExerciseDto;
import com.fitexa.dtos.RoutineTemplateRequestDto;
import com.fitexa.dtos.RoutineTemplateResponseDto;
import com.fitexa.dtos.DashboardPageResponseDto;
import com.fitexa.entities.ExerciseEntity;
import com.fitexa.entities.RoutineTemplateEntity;
import com.fitexa.exeptions.BusinessValidationException;
import com.fitexa.exeptions.ResourceNotFoundException;
import com.fitexa.repository.ExerciseRepository;
import com.fitexa.repository.RoutineTemplateRepository;
import com.fitexa.services.RoutineTemplateService;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoutineTemplateServiceImpl implements RoutineTemplateService {

    private static final int MAX_PAGE_SIZE = 100;

    private final RoutineTemplateRepository routineTemplateRepository;
    private final ExerciseRepository exerciseRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RoutineTemplateResponseDto> findAll() {
        return routineTemplateRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardPageResponseDto<RoutineTemplateResponseDto> findPage(
        String name,
        Integer minExercises,
        Integer minStudents,
        int page,
        int size
    ) {
        Integer normalizedMinExercises = minExercises == null || minExercises <= 0 ? null : minExercises;
        Integer normalizedMinStudents = minStudents == null || minStudents <= 0 ? null : minStudents;

        Page<RoutineTemplateResponseDto> result = routineTemplateRepository.findForTable(
            clean(name),
            normalizedMinExercises,
            normalizedMinStudents,
            LocalDate.now(),
            PageRequest.of(
                sanitizePage(page),
                sanitizeSize(size),
                Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.ASC, "name"))
            )
        ).map(this::toDto);

        return toPageResponse(result);
    }

    @Override
    @Transactional(readOnly = true)
    public RoutineTemplateResponseDto findById(Long id) {
        return toDto(findEntity(id));
    }

    @Override
    @Transactional
    public RoutineTemplateResponseDto create(RoutineTemplateRequestDto request) {
        Set<ExerciseEntity> exercises = resolveExercises(request.getExerciseIds());
        RoutineTemplateEntity entity = RoutineTemplateEntity.builder()
            .name(request.getName())
            .createdAt(request.getCreatedAt() == null ? LocalDate.now() : request.getCreatedAt())
            .exercises(exercises)
            .build();
        return toDto(routineTemplateRepository.save(entity));
    }

    @Override
    @Transactional
    public RoutineTemplateResponseDto update(Long id, RoutineTemplateRequestDto request) {
        RoutineTemplateEntity entity = findEntity(id);
        entity.setName(request.getName());
        entity.setCreatedAt(request.getCreatedAt() == null ? entity.getCreatedAt() : request.getCreatedAt());
        entity.setExercises(resolveExercises(request.getExerciseIds()));
        return toDto(routineTemplateRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        routineTemplateRepository.delete(findEntity(id));
    }

    private Set<ExerciseEntity> resolveExercises(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessValidationException("La rutina debe tener al menos un ejercicio.");
        }
        Set<ExerciseEntity> exercises = new LinkedHashSet<>();
        ids.forEach(exerciseId -> exercises.add(
            exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ResourceNotFoundException("Ejercicio no encontrado: " + exerciseId))
        ));
        return exercises;
    }

    private RoutineTemplateEntity findEntity(Long id) {
        return routineTemplateRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Rutina no encontrada: " + id));
    }

    private RoutineTemplateResponseDto toDto(RoutineTemplateEntity entity) {
        return RoutineTemplateResponseDto.builder()
            .id(entity.getId())
            .name(entity.getName())
            .createdAt(entity.getCreatedAt())
            .exercises(entity.getExercises().stream()
                .map(ex -> RoutineTemplateExerciseDto.builder()
                    .exerciseId(ex.getId())
                    .exerciseName(ex.getName())
                    .build())
                .toList())
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
