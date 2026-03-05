package com.fitexa.dtos;

import com.fitexa.entities.WeekDay;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoutineAssignmentResponseDto {
    private Long id;
    private Long userId;
    private String userName;
    private Long routineTemplateId;
    private String routineTemplateName;
    private Set<WeekDay> days;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<RoutineAssignmentExerciseResponseDto> exercisePlans;
}
