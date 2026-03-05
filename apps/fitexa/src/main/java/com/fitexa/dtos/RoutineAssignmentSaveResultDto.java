package com.fitexa.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoutineAssignmentSaveResultDto {
    private RoutineAssignmentResponseDto assignment;
    private String warning;
}
