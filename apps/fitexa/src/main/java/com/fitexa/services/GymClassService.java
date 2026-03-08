package com.fitexa.services;

import com.fitexa.dtos.GymClassRequestDto;
import com.fitexa.dtos.GymClassResponseDto;
import com.fitexa.dtos.DashboardPageResponseDto;
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
    GymClassResponseDto create(GymClassRequestDto request);
    GymClassResponseDto update(Long id, GymClassRequestDto request);
    void delete(Long id);
}
