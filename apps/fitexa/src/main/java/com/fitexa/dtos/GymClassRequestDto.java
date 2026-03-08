package com.fitexa.dtos;

import com.fitexa.entities.WeekDay;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.time.LocalTime;
import java.util.Set;
import lombok.Data;

@Data
public class GymClassRequestDto {
    @NotBlank
    private String className;

    private Long activityId;
    private Integer maxCapacity;

    private Long teacherId;

    @NotEmpty
    private Set<WeekDay> weekDays;

    private Boolean allDay;

    private LocalTime startTime;

    private LocalTime endTime;
}
