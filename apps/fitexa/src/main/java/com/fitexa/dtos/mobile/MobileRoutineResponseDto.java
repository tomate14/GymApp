package com.fitexa.dtos.mobile;

import com.fitexa.entities.WeekDay;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MobileRoutineResponseDto {
    private Long id;
    private String routineTemplateName;
    private String status;
    private Set<WeekDay> days;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<MobileRoutineExerciseResponseDto> exercises;
}
