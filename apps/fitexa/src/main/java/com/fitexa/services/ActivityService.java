package com.fitexa.services;

import com.fitexa.dtos.ActivityRequestDto;
import com.fitexa.dtos.ActivityResponseDto;
import java.util.List;

public interface ActivityService {
    List<ActivityResponseDto> findAll();
    ActivityResponseDto findById(Long id);
    ActivityResponseDto create(ActivityRequestDto request);
    ActivityResponseDto update(Long id, ActivityRequestDto request);
    void delete(Long id);
}
