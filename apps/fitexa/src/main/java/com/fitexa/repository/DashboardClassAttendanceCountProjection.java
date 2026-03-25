package com.fitexa.repository;

import java.time.LocalDate;

public interface DashboardClassAttendanceCountProjection {
    Long getGymClassId();
    LocalDate getTargetDate();
    Long getAttendanceCount();
}
