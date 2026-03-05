package com.fitexa.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoutineTemplateExerciseDto {
    private Long exerciseId;
    private String exerciseName;
}
