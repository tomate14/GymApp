package com.fitexa.services;

import com.fitexa.dtos.ExerciseRequestDto;
import com.fitexa.dtos.ExerciseResponseDto;
import com.fitexa.dtos.DashboardPageResponseDto;
import java.util.List;

public interface ExerciseService {
    List<ExerciseResponseDto> findAll();
    DashboardPageResponseDto<ExerciseResponseDto> findPage(String name, Long activityId, Long machineId, int page, int size);
    ExerciseResponseDto findById(Long id);
    ExerciseResponseDto create(ExerciseRequestDto request);
    ExerciseResponseDto update(Long id, ExerciseRequestDto request);
    void delete(Long id);
}
