package com.fitexa.services;

import com.fitexa.dtos.RoutineTemplateRequestDto;
import com.fitexa.dtos.RoutineTemplateResponseDto;
import java.util.List;

public interface RoutineTemplateService {
    List<RoutineTemplateResponseDto> findAll();
    RoutineTemplateResponseDto findById(Long id);
    RoutineTemplateResponseDto create(RoutineTemplateRequestDto request);
    RoutineTemplateResponseDto update(Long id, RoutineTemplateRequestDto request);
    void delete(Long id);
}
