package com.fitexa.repository;

public interface DashboardMachineUsageProjection {
    Long getMachineId();
    String getMachineName();
    Double getUsageCount();
}
