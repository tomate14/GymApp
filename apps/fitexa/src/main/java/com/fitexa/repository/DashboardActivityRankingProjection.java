package com.fitexa.repository;

public interface DashboardActivityRankingProjection {
    Long getActivityId();
    String getActivityName();
    Long getClassesCount();
    Long getTotalEnrolled();
    Double getAverageOccupancyPercent();
}
