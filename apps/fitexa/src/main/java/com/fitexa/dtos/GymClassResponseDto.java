package com.fitexa.dtos;

import com.fitexa.entities.WeekDay;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GymClassResponseDto {
    private Long id;
    private String className;
    private Long activityId;
    private String activityName;
    private Integer maxCapacity;
    private Integer enrolled;
    private Long teacherId;
    private String teacherName;
    private LocalDate date;
    private Boolean allDay;
    private LocalTime startTime;
    private LocalTime endTime;
    private Set<WeekDay> weekDays;
}
