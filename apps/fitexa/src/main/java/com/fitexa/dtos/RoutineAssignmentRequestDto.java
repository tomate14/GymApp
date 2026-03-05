package com.fitexa.dtos;

import com.fitexa.entities.WeekDay;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import lombok.Data;

@Data
public class RoutineAssignmentRequestDto {
    @NotNull
    private Long userId;

    @NotNull
    private Long routineTemplateId;

    @NotNull
    private Set<WeekDay> days;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @NotNull
    private List<RoutineAssignmentExerciseRequestDto> exercisePlans;
}
