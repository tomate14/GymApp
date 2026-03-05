package com.fitexa.dtos;

import com.fitexa.entities.PrescriptionMode;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoutineAssignmentExerciseRequestDto {
    @NotNull
    private Long exerciseId;

    @NotNull
    private Integer sets;

    @NotNull
    private PrescriptionMode prescriptionMode;

    private Integer reps;
    private Double weight;
    private Integer minReps;
    private Integer maxReps;
    private Double minWeight;
    private Double maxWeight;
}
