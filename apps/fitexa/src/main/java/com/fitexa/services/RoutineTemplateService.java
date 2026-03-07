package com.fitexa.services;

import com.fitexa.dtos.RoutineTemplateRequestDto;
import com.fitexa.dtos.RoutineTemplateResponseDto;
import com.fitexa.dtos.DashboardPageResponseDto;
import java.util.List;

public interface RoutineTemplateService {
    List<RoutineTemplateResponseDto> findAll();
    DashboardPageResponseDto<RoutineTemplateResponseDto> findPage(
        String name,
        Integer minExercises,
        Integer minStudents,
        int page,
        int size
    );
    RoutineTemplateResponseDto findById(Long id);
    RoutineTemplateResponseDto create(RoutineTemplateRequestDto request);
    RoutineTemplateResponseDto update(Long id, RoutineTemplateRequestDto request);
    void delete(Long id);
}
