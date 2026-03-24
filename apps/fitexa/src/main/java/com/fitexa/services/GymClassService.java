package com.fitexa.services;

import com.fitexa.dtos.GymClassAttendanceResponseDto;
import com.fitexa.dtos.GymClassAttendanceUpdateRequestDto;
import com.fitexa.dtos.GymClassRequestDto;
import com.fitexa.dtos.GymClassResponseDto;
import com.fitexa.dtos.DashboardPageResponseDto;
import java.time.LocalDate;
import java.util.List;

public interface GymClassService {
    List<GymClassResponseDto> findAll();
    DashboardPageResponseDto<GymClassResponseDto> findPage(
        String className,
        Long activityId,
        Long teacherId,
        String weekDay,
        int page,
        int size
    );
    GymClassResponseDto findById(Long id);
    GymClassAttendanceResponseDto getAttendance(Long id, LocalDate targetDate);
    GymClassAttendanceResponseDto updateAttendance(Long id, LocalDate targetDate, GymClassAttendanceUpdateRequestDto request);
    GymClassResponseDto create(GymClassRequestDto request);
    GymClassResponseDto update(Long id, GymClassRequestDto request);
    void delete(Long id);
}
