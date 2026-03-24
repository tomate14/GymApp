package com.fitexa.dtos;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GymClassAttendanceResponseDto {
    private Long classId;
    private String className;
    private LocalDate targetDate;
    private List<GymClassAttendanceStudentDto> students;
}
