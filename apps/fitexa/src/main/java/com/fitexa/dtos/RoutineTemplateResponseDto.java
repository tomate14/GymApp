package com.fitexa.dtos;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoutineTemplateResponseDto {
    private Long id;
    private String name;
    private LocalDate createdAt;
    private List<RoutineTemplateExerciseDto> exercises;
}
