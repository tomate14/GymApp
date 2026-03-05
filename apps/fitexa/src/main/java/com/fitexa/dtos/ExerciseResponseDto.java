package com.fitexa.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExerciseResponseDto {
    private Long id;
    private String name;
    private Long activityId;
    private String activityName;
    private Long machineId;
    private String machineName;
    private String videoUrl;
}
