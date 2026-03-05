package com.fitexa.services;

import com.fitexa.dtos.ExerciseRequestDto;
import com.fitexa.dtos.ExerciseResponseDto;
import java.util.List;

public interface ExerciseService {
    List<ExerciseResponseDto> findAll();
    ExerciseResponseDto findById(Long id);
    ExerciseResponseDto create(ExerciseRequestDto request);
    ExerciseResponseDto update(Long id, ExerciseRequestDto request);
    void delete(Long id);
}
