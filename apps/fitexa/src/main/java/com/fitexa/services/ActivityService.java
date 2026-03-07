package com.fitexa.services;

import com.fitexa.dtos.ActivityRequestDto;
import com.fitexa.dtos.ActivityResponseDto;
import com.fitexa.dtos.DashboardPageResponseDto;
import java.util.List;

public interface ActivityService {
    List<ActivityResponseDto> findAll();
    DashboardPageResponseDto<ActivityResponseDto> findPage(String name, String description, int page, int size);
    ActivityResponseDto findById(Long id);
    ActivityResponseDto create(ActivityRequestDto request);
    ActivityResponseDto update(Long id, ActivityRequestDto request);
    void delete(Long id);
}
