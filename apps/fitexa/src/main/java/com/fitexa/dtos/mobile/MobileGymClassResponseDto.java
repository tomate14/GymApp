package com.fitexa.dtos.mobile;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MobileGymClassResponseDto {
    private Long id;
    private String className;
    private String activityName;
    private String teacherName;
    private LocalDate targetDate;
    private Boolean allDay;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer maxCapacity;
    private Integer enrolled;
    private Integer availableSpots;
    private boolean subscribed;
    private boolean ownClass;
    private boolean activityAllowed;
    private boolean canSubscribe;
    private String restrictionReason;
}
