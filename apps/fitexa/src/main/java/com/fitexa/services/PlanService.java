package com.fitexa.services;

import com.fitexa.dtos.PlanRequestDto;
import com.fitexa.dtos.PlanResponseDto;
import java.util.List;

public interface PlanService {
    List<PlanResponseDto> findAll();
    List<PlanResponseDto> findActive();
    PlanResponseDto findById(Long id);
    PlanResponseDto create(PlanRequestDto request);
    PlanResponseDto update(Long id, PlanRequestDto request);
    void delete(Long id);
}
