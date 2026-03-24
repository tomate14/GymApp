package com.fitexa.dtos;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GymClassAttendanceStudentDto {
    private Long subscriptionId;
    private Long studentId;
    private String studentName;
    private String dni;
    private String phone;
    private Boolean attendancePresent;
    private LocalDateTime attendanceMarkedAt;
}
