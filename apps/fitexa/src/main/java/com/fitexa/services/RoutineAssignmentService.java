package com.fitexa.services;

import com.fitexa.dtos.RoutineAssignmentRequestDto;
import com.fitexa.dtos.RoutineAssignmentResponseDto;
import com.fitexa.dtos.RoutineAssignmentSaveResultDto;
import com.fitexa.dtos.DashboardPageResponseDto;
import java.util.List;

public interface RoutineAssignmentService {
    List<RoutineAssignmentResponseDto> findAll();
    DashboardPageResponseDto<RoutineAssignmentResponseDto> findPage(
        String student,
        Long templateId,
        boolean currentOnly,
        int page,
        int size
    );
    RoutineAssignmentResponseDto findById(Long id);
    RoutineAssignmentSaveResultDto create(RoutineAssignmentRequestDto request);
    RoutineAssignmentSaveResultDto update(Long id, RoutineAssignmentRequestDto request);
    void delete(Long id);
}
