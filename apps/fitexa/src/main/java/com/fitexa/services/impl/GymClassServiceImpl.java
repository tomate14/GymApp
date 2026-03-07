package com.fitexa.services.impl;

import com.fitexa.dtos.GymClassRequestDto;
import com.fitexa.dtos.GymClassResponseDto;
import com.fitexa.dtos.DashboardPageResponseDto;
import com.fitexa.entities.ActivityEntity;
import com.fitexa.entities.GymClassEntity;
import com.fitexa.entities.UserEntity;
import com.fitexa.entities.UserType;
import com.fitexa.exeptions.BusinessValidationException;
import com.fitexa.exeptions.ResourceNotFoundException;
import com.fitexa.repository.ActivityRepository;
import com.fitexa.repository.GymClassRepository;
import com.fitexa.repository.GymClassSubscriptionRepository;
import com.fitexa.repository.UserRepository;
import com.fitexa.services.GymClassService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GymClassServiceImpl implements GymClassService {

    private static final int MAX_PAGE_SIZE = 100;

    private final GymClassRepository gymClassRepository;
    private final GymClassSubscriptionRepository gymClassSubscriptionRepository;
    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<GymClassResponseDto> findAll() {
        return gymClassRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardPageResponseDto<GymClassResponseDto> findPage(
        String className,
        Long activityId,
        Long teacherId,
        LocalDate dateFrom,
        LocalDate dateTo,
        int page,
        int size
    ) {
        Page<GymClassResponseDto> result = gymClassRepository.findForTable(
            clean(className),
            activityId,
            teacherId,
            dateFrom,
            dateTo,
            PageRequest.of(
                sanitizePage(page),
                sanitizeSize(size),
                Sort.by(Sort.Direction.ASC, "date").and(Sort.by(Sort.Direction.ASC, "time"))
            )
        ).map(this::toDto);

        return toPageResponse(result);
    }

    @Override
    @Transactional(readOnly = true)
    public GymClassResponseDto findById(Long id) {
        return toDto(findEntity(id));
    }

    @Override
    @Transactional
    public GymClassResponseDto create(GymClassRequestDto request) {
        GymClassEntity entity = GymClassEntity.builder()
            .className(request.getClassName())
            .activity(findActivity(request.getActivityId()))
            .maxCapacity(request.getMaxCapacity())
            .enrolled(0)
            .teacher(findTeacher(request.getTeacherId()))
            .date(request.getDate())
            .time(request.getTime())
            .build();
        return toDto(gymClassRepository.save(entity));
    }

    @Override
    @Transactional
    public GymClassResponseDto update(Long id, GymClassRequestDto request) {
        GymClassEntity entity = findEntity(id);
        entity.setClassName(request.getClassName());
        entity.setActivity(findActivity(request.getActivityId()));
        entity.setMaxCapacity(request.getMaxCapacity());
        entity.setTeacher(findTeacher(request.getTeacherId()));
        entity.setDate(request.getDate());
        entity.setTime(request.getTime());
        entity.setEnrolled(resolveEnrolledCount(entity));
        return toDto(gymClassRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        gymClassRepository.delete(findEntity(id));
    }

    private GymClassEntity findEntity(Long id) {
        return gymClassRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Clase no encontrada: " + id));
    }

    private ActivityEntity findActivity(Long id) {
        if (id == null) {
            return null;
        }
        return activityRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada: " + id));
    }

    private UserEntity findTeacher(Long id) {
        if (id == null) {
            return null;
        }
        UserEntity teacher = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Profesor no encontrado: " + id));
        if (teacher.getUserType() != UserType.PROFESOR) {
            throw new BusinessValidationException("El usuario seleccionado no es profesor.");
        }
        return teacher;
    }

    private GymClassResponseDto toDto(GymClassEntity entity) {
        int enrolled = resolveEnrolledCount(entity);

        return GymClassResponseDto.builder()
            .id(entity.getId())
            .className(entity.getClassName())
            .activityId(entity.getActivity() == null ? null : entity.getActivity().getId())
            .activityName(entity.getActivity() == null ? null : entity.getActivity().getName())
            .maxCapacity(entity.getMaxCapacity())
            .enrolled(enrolled)
            .teacherId(entity.getTeacher() == null ? null : entity.getTeacher().getId())
            .teacherName(entity.getTeacher() == null ? null : entity.getTeacher().getFirstName() + " " + entity.getTeacher().getLastName())
            .date(entity.getDate())
            .time(entity.getTime())
            .build();
    }

    private int resolveEnrolledCount(GymClassEntity entity) {
        if (entity.getId() == null) {
            return 0;
        }

        long subscribedCount = gymClassSubscriptionRepository.countByGymClassId(entity.getId());
        if (subscribedCount == 0L) {
            return entity.getEnrolled() == null ? 0 : entity.getEnrolled();
        }

        return Math.toIntExact(subscribedCount);
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
