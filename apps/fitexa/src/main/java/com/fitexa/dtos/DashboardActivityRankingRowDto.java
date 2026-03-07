package com.fitexa.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardActivityRankingRowDto {
    private Long activityId;
    private String activityName;
    private long classesCount;
    private long totalEnrolled;
    private double averageOccupancyPercent;
}
