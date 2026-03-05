package com.fitexa.services;

import com.fitexa.dtos.RoutineAssignmentRequestDto;
import com.fitexa.dtos.RoutineAssignmentResponseDto;
import com.fitexa.dtos.RoutineAssignmentSaveResultDto;
import java.util.List;

public interface RoutineAssignmentService {
    List<RoutineAssignmentResponseDto> findAll();
    RoutineAssignmentResponseDto findById(Long id);
    RoutineAssignmentSaveResultDto create(RoutineAssignmentRequestDto request);
    RoutineAssignmentSaveResultDto update(Long id, RoutineAssignmentRequestDto request);
    void delete(Long id);
}
