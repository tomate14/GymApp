package com.fitexa.services.impl.dashboard;

import com.fitexa.entities.GymClassEntity;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DashboardClassOccurrenceMetric {
    GymClassEntity gymClass;
    LocalDate date;
    String activityName;
    Long activityId;
    String teacherName;
    Long teacherId;
    boolean allDay;
    LocalTime startTime;
    LocalTime endTime;
    int enrolled;
    int attendees;
    Integer maxCapacity;
    double occupancyPercent;

    public boolean isAtCapacity() {
        return maxCapacity != null && maxCapacity > 0 && enrolled >= maxCapacity;
    }

    public boolean hasTeacherAssigned() {
        return teacherId != null;
    }
}
