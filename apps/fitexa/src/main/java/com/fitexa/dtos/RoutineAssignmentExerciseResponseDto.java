package com.fitexa.dtos;

import com.fitexa.entities.PrescriptionMode;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoutineAssignmentExerciseResponseDto {
    private Long exerciseId;
    private String exerciseName;
    private Integer sets;
    private PrescriptionMode prescriptionMode;
    private Integer reps;
    private Double weight;
    private Integer minReps;
    private Integer maxReps;
    private Double minWeight;
    private Double maxWeight;
}
