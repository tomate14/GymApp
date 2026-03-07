package com.fitexa.dtos;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardClassRowDto {
    private Long classId;
    private String className;
    private String activityName;
    private String teacherName;
    private LocalDate date;
    private LocalTime time;
    private Integer enrolled;
    private Integer maxCapacity;
    private double occupancyPercent;
}
