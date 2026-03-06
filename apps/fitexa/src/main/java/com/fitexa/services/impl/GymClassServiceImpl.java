package com.fitexa.services.impl;

import com.fitexa.dtos.GymClassRequestDto;
import com.fitexa.dtos.GymClassResponseDto;
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
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GymClassServiceImpl implements GymClassService {

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
}
