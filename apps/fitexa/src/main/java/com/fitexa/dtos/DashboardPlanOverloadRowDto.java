package com.fitexa.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardPlanOverloadRowDto {
    private Long userId;
    private String studentName;
    private String dni;
    private String planName;
    private int maxDaysPerWeek;
    private int assignedDays;
    private int extraDays;
}
