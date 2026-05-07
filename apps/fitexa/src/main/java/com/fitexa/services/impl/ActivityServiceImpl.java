package com.fitexa.services.impl;

import com.fitexa.dtos.ActivityRequestDto;
import com.fitexa.dtos.ActivityResponseDto;
import com.fitexa.dtos.DashboardPageResponseDto;
import com.fitexa.entities.ActivityEntity;
import com.fitexa.exeptions.ResourceNotFoundException;
import com.fitexa.repository.ActivityRepository;
import com.fitexa.services.AuthenticatedUserService;
import com.fitexa.services.ActivityService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private static final int MAX_PAGE_SIZE = 100;

    private final ActivityRepository activityRepository;
    private final AuthenticatedUserService authenticatedUserService;

    @Override
    @Transactional(readOnly = true)
    public List<ActivityResponseDto> findAll() {
        return activityRepository.findAllByClientId(currentClientId()).stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardPageResponseDto<ActivityResponseDto> findPage(String name, String description, int page, int size) {
        Page<ActivityResponseDto> result = activityRepository.findForTable(
            currentClientId(),
            clean(name),
            clean(description),
            PageRequest.of(sanitizePage(page), sanitizeSize(size), Sort.by(Sort.Direction.ASC, "name"))
        ).map(this::toDto);

        return toPageResponse(result);
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
        entity.setClient(authenticatedUserService.getCurrentClient());
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
        return activityRepository.findByIdAndClientId(id, currentClientId())
            .orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada: " + id));
    }

    private Long currentClientId() {
        return authenticatedUserService.getCurrentClientId();
    }

    private ActivityResponseDto toDto(ActivityEntity entity) {
        return ActivityResponseDto.builder()
            .id(entity.getId())
            .name(entity.getName())
            .description(entity.getDescription())
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
