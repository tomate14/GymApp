package com.fitexa.services.impl;

import com.fitexa.dtos.ExerciseRequestDto;
import com.fitexa.dtos.ExerciseResponseDto;
import com.fitexa.dtos.DashboardPageResponseDto;
import com.fitexa.entities.ActivityEntity;
import com.fitexa.entities.ExerciseEntity;
import com.fitexa.entities.MachineEntity;
import com.fitexa.exeptions.ResourceNotFoundException;
import com.fitexa.repository.ActivityRepository;
import com.fitexa.repository.ExerciseRepository;
import com.fitexa.repository.MachineRepository;
import com.fitexa.services.AuthenticatedUserService;
import com.fitexa.services.ExerciseService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExerciseServiceImpl implements ExerciseService {

    private static final int MAX_PAGE_SIZE = 100;

    private final ExerciseRepository exerciseRepository;
    private final ActivityRepository activityRepository;
    private final MachineRepository machineRepository;
    private final AuthenticatedUserService authenticatedUserService;

    @Override
    @Transactional(readOnly = true)
    public List<ExerciseResponseDto> findAll() {
        return exerciseRepository.findAllByClientId(currentClientId()).stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardPageResponseDto<ExerciseResponseDto> findPage(
        String name,
        Long activityId,
        Long machineId,
        int page,
        int size
    ) {
        Page<ExerciseResponseDto> result = exerciseRepository.findForTable(
            currentClientId(),
            clean(name),
            activityId,
            machineId,
            PageRequest.of(sanitizePage(page), sanitizeSize(size), Sort.by(Sort.Direction.ASC, "name"))
        ).map(this::toDto);

        return toPageResponse(result);
    }

    @Override
    @Transactional(readOnly = true)
    public ExerciseResponseDto findById(Long id) {
        return toDto(findEntity(id));
    }

    @Override
    @Transactional
    public ExerciseResponseDto create(ExerciseRequestDto request) {
        ExerciseEntity entity = ExerciseEntity.builder()
            .name(request.getName())
            .activity(findActivity(request.getActivityId()))
            .machine(findMachine(request.getMachineId()))
            .videoUrl(request.getVideoUrl())
            .build();
        entity.setClient(authenticatedUserService.getCurrentClient());
        return toDto(exerciseRepository.save(entity));
    }

    @Override
    @Transactional
    public ExerciseResponseDto update(Long id, ExerciseRequestDto request) {
        ExerciseEntity entity = findEntity(id);
        entity.setName(request.getName());
        entity.setActivity(findActivity(request.getActivityId()));
        entity.setMachine(findMachine(request.getMachineId()));
        entity.setVideoUrl(request.getVideoUrl());
        return toDto(exerciseRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        exerciseRepository.delete(findEntity(id));
    }

    private ExerciseEntity findEntity(Long id) {
        return exerciseRepository.findByIdAndClientId(id, currentClientId())
            .orElseThrow(() -> new ResourceNotFoundException("Ejercicio no encontrado: " + id));
    }

    private ActivityEntity findActivity(Long id) {
        if (id == null) {
            return null;
        }
        return activityRepository.findByIdAndClientId(id, currentClientId())
            .orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada: " + id));
    }

    private MachineEntity findMachine(Long id) {
        if (id == null) {
            return null;
        }
        return machineRepository.findByIdAndClientId(id, currentClientId())
            .orElseThrow(() -> new ResourceNotFoundException("Maquina no encontrada: " + id));
    }

    private Long currentClientId() {
        return authenticatedUserService.getCurrentClientId();
    }

    private ExerciseResponseDto toDto(ExerciseEntity entity) {
        return ExerciseResponseDto.builder()
            .id(entity.getId())
            .name(entity.getName())
            .activityId(entity.getActivity() == null ? null : entity.getActivity().getId())
            .activityName(entity.getActivity() == null ? null : entity.getActivity().getName())
            .machineId(entity.getMachine() == null ? null : entity.getMachine().getId())
            .machineName(entity.getMachine() == null ? null : entity.getMachine().getName())
            .videoUrl(entity.getVideoUrl())
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
