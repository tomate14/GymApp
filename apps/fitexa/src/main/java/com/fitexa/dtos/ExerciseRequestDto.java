package com.fitexa.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ExerciseRequestDto {
    @NotBlank
    private String name;

    private Long activityId;
    private Long machineId;

    @NotBlank
    private String videoUrl;
}
