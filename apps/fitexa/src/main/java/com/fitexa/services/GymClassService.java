package com.fitexa.services;

import com.fitexa.dtos.GymClassRequestDto;
import com.fitexa.dtos.GymClassResponseDto;
import java.util.List;

public interface GymClassService {
    List<GymClassResponseDto> findAll();
    GymClassResponseDto findById(Long id);
    GymClassResponseDto create(GymClassRequestDto request);
    GymClassResponseDto update(Long id, GymClassRequestDto request);
    void delete(Long id);
}
