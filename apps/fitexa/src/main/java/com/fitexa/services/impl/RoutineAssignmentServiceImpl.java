package com.fitexa.services.impl;

import com.fitexa.dtos.RoutineAssignmentExerciseRequestDto;
import com.fitexa.dtos.RoutineAssignmentExerciseResponseDto;
import com.fitexa.dtos.RoutineAssignmentRequestDto;
import com.fitexa.dtos.RoutineAssignmentResponseDto;
import com.fitexa.dtos.RoutineAssignmentSaveResultDto;
import com.fitexa.entities.ExerciseEntity;
import com.fitexa.entities.PrescriptionMode;
import com.fitexa.entities.RoutineAssignmentEntity;
import com.fitexa.entities.RoutineAssignmentExerciseEntity;
import com.fitexa.entities.RoutineTemplateEntity;
import com.fitexa.entities.UserEntity;
import com.fitexa.entities.UserType;
import com.fitexa.exeptions.BusinessValidationException;
import com.fitexa.exeptions.ResourceNotFoundException;
import com.fitexa.repository.ExerciseRepository;
import com.fitexa.repository.RoutineAssignmentRepository;
import com.fitexa.repository.RoutineTemplateRepository;
import com.fitexa.repository.UserRepository;
import com.fitexa.services.RoutineAssignmentService;
import com.fitexa.services.RoutinePlanValidationStrategy;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoutineAssignmentServiceImpl implements RoutineAssignmentService {

    private final RoutineAssignmentRepository routineAssignmentRepository;
    private final RoutineTemplateRepository routineTemplateRepository;
    private final UserRepository userRepository;
    private final ExerciseRepository exerciseRepository;
    private final RoutinePlanValidationStrategy routinePlanValidationStrategy;

    @Override
    @Transactional(readOnly = true)
    public List<RoutineAssignmentResponseDto> findAll() {
        return routineAssignmentRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RoutineAssignmentResponseDto findById(Long id) {
        return toDto(findEntity(id));
    }

    @Override
    @Transactional
    public RoutineAssignmentSaveResultDto create(RoutineAssignmentRequestDto request) {
        return saveInternal(null, request);
    }

    @Override
    @Transactional
    public RoutineAssignmentSaveResultDto update(Long id, RoutineAssignmentRequestDto request) {
        return saveInternal(id, request);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        routineAssignmentRepository.delete(findEntity(id));
    }

    private RoutineAssignmentSaveResultDto saveInternal(Long id, RoutineAssignmentRequestDto request) {
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BusinessValidationException("La fecha de fin no puede ser anterior a la fecha de inicio.");
        }
        if (request.getDays() == null || request.getDays().isEmpty()) {
            throw new BusinessValidationException("La asignacion debe tener al menos un dia de ejecucion.");
        }
        if (request.getExercisePlans() == null || request.getExercisePlans().isEmpty()) {
            throw new BusinessValidationException("La asignacion debe tener ejercicios.");
        }

        UserEntity student = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("Alumno no encontrado: " + request.getUserId()));
        if (student.getUserType() != UserType.ALUMNO) {
            throw new BusinessValidationException("La asignacion de rutinas solo aplica para alumnos.");
        }

        RoutineTemplateEntity routineTemplate = routineTemplateRepository.findById(request.getRoutineTemplateId())
            .orElseThrow(() -> new ResourceNotFoundException("Rutina no encontrada: " + request.getRoutineTemplateId()));

        RoutineAssignmentEntity entity = id == null ? new RoutineAssignmentEntity() : findEntity(id);
        entity.setUser(student);
        entity.setRoutineTemplate(routineTemplate);
        entity.setDays(request.getDays());
        entity.setStartDate(request.getStartDate());
        entity.setEndDate(request.getEndDate());
        List<RoutineAssignmentExerciseEntity> mappedExercisePlans =
            mapExercisePlans(entity, routineTemplate, request.getExercisePlans());
        entity.getExercisePlans().clear();
        entity.getExercisePlans().addAll(mappedExercisePlans);

        RoutineAssignmentEntity saved = routineAssignmentRepository.save(entity);
        List<RoutineAssignmentEntity> overlaps = routineAssignmentRepository.findOverlappingAssignments(
            student.getId(), saved.getStartDate(), saved.getEndDate(), saved.getId()
        );
        String warning = routinePlanValidationStrategy.validate(student, saved, overlaps).orElse(null);

        return RoutineAssignmentSaveResultDto.builder()
            .assignment(toDto(saved))
            .warning(warning)
            .build();
    }

    private List<RoutineAssignmentExerciseEntity> mapExercisePlans(
        RoutineAssignmentEntity assignment,
        RoutineTemplateEntity routineTemplate,
        List<RoutineAssignmentExerciseRequestDto> requestItems
    ) {
        Set<Long> templateExerciseIds = routineTemplate.getExercises().stream().map(ExerciseEntity::getId).collect(java.util.stream.Collectors.toSet());
        List<RoutineAssignmentExerciseEntity> mapped = new ArrayList<>();
        for (RoutineAssignmentExerciseRequestDto item : requestItems) {
            if (!templateExerciseIds.contains(item.getExerciseId())) {
                throw new BusinessValidationException("El ejercicio " + item.getExerciseId() + " no pertenece a la rutina seleccionada.");
            }

            ExerciseEntity exercise = exerciseRepository.findById(item.getExerciseId())
                .orElseThrow(() -> new ResourceNotFoundException("Ejercicio no encontrado: " + item.getExerciseId()));

            RoutineAssignmentExerciseEntity detail = RoutineAssignmentExerciseEntity.builder()
                .assignment(assignment)
                .exercise(exercise)
                .exerciseName(exercise.getName())
                .sets(Math.max(1, Objects.requireNonNullElse(item.getSets(), 1)))
                .prescriptionMode(item.getPrescriptionMode())
                .reps(item.getReps())
                .weight(item.getWeight())
                .minReps(item.getMinReps())
                .maxReps(item.getMaxReps())
                .minWeight(item.getMinWeight())
                .maxWeight(item.getMaxWeight())
                .build();

            normalizeByMode(detail);
            mapped.add(detail);
        }
        return mapped;
    }

    private void normalizeByMode(RoutineAssignmentExerciseEntity detail) {
        if (detail.getPrescriptionMode() == PrescriptionMode.FIJO) {
            detail.setReps(Math.max(1, Objects.requireNonNullElse(detail.getReps(), 1)));
            detail.setWeight(Math.max(0d, Objects.requireNonNullElse(detail.getWeight(), 0d)));
            detail.setMinReps(null);
            detail.setMaxReps(null);
            detail.setMinWeight(null);
            detail.setMaxWeight(null);
            return;
        }

        int minReps = Math.max(1, Objects.requireNonNullElse(detail.getMinReps(), 1));
        int maxReps = Math.max(minReps, Objects.requireNonNullElse(detail.getMaxReps(), minReps));
        double minWeight = Math.max(0d, Objects.requireNonNullElse(detail.getMinWeight(), 0d));
        double maxWeight = Math.max(minWeight, Objects.requireNonNullElse(detail.getMaxWeight(), minWeight));

        detail.setReps(null);
        detail.setWeight(null);
        detail.setMinReps(minReps);
        detail.setMaxReps(maxReps);
        detail.setMinWeight(minWeight);
        detail.setMaxWeight(maxWeight);
    }

    private RoutineAssignmentEntity findEntity(Long id) {
        return routineAssignmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Asignacion no encontrada: " + id));
    }

    private RoutineAssignmentResponseDto toDto(RoutineAssignmentEntity entity) {
        return RoutineAssignmentResponseDto.builder()
            .id(entity.getId())
            .userId(entity.getUser().getId())
            .userName(entity.getUser().getFirstName() + " " + entity.getUser().getLastName())
            .routineTemplateId(entity.getRoutineTemplate().getId())
            .routineTemplateName(entity.getRoutineTemplate().getName())
            .days(entity.getDays())
            .startDate(entity.getStartDate())
            .endDate(entity.getEndDate())
            .exercisePlans(entity.getExercisePlans().stream().map(item ->
                RoutineAssignmentExerciseResponseDto.builder()
                    .exerciseId(item.getExercise().getId())
                    .exerciseName(item.getExerciseName())
                    .sets(item.getSets())
                    .prescriptionMode(item.getPrescriptionMode())
                    .reps(item.getReps())
                    .weight(item.getWeight())
                    .minReps(item.getMinReps())
                    .maxReps(item.getMaxReps())
                    .minWeight(item.getMinWeight())
                    .maxWeight(item.getMaxWeight())
                    .build()
            ).toList())
            .build();
    }
}
