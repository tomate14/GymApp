package com.fitexa.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Data;

@Data
public class GymClassRequestDto {
    @NotBlank
    private String className;

    private Long activityId;
    private Integer maxCapacity;

    @NotNull
    private Integer enrolled;

    private Long teacherId;

    @NotNull
    private LocalDate date;

    @NotNull
    private LocalTime time;
}
