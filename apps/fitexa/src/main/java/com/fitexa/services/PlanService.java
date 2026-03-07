package com.fitexa.services;

import com.fitexa.dtos.PlanRequestDto;
import com.fitexa.dtos.PlanResponseDto;
import com.fitexa.dtos.DashboardPageResponseDto;
import java.util.List;

public interface PlanService {
    List<PlanResponseDto> findAll();
    DashboardPageResponseDto<PlanResponseDto> findPage(String name, String status, int page, int size);
    List<PlanResponseDto> findActive();
    PlanResponseDto findById(Long id);
    PlanResponseDto create(PlanRequestDto request);
    PlanResponseDto update(Long id, PlanRequestDto request);
    void delete(Long id);
}
