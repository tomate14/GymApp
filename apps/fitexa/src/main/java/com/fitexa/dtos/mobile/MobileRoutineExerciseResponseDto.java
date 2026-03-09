package com.fitexa.dtos.mobile;

import com.fitexa.entities.PrescriptionMode;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MobileRoutineExerciseResponseDto {
    private Long exerciseId;
    private String exerciseName;
    private String activityName;
    private String machineName;
    private String videoUrl;
    private Integer sets;
    private PrescriptionMode prescriptionMode;
    private Integer reps;
    private Double weight;
    private Integer minReps;
    private Integer maxReps;
    private Double minWeight;
    private Double maxWeight;
}
