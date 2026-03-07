package com.fitexa.repository;

public interface DashboardPlanOverloadProjection {
    Long getUserId();
    String getFirstName();
    String getLastName();
    String getDni();
    String getPlanName();
    Integer getMaxDaysPerWeek();
    Long getAssignedDays();
}
