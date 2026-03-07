package com.fitexa.services;

import com.fitexa.dtos.MachineRequestDto;
import com.fitexa.dtos.MachineResponseDto;
import com.fitexa.dtos.DashboardPageResponseDto;
import java.util.List;

public interface MachineService {
    List<MachineResponseDto> findAll();
    DashboardPageResponseDto<MachineResponseDto> findPage(String name, boolean onlyInUse, int page, int size);
    MachineResponseDto findById(Long id);
    MachineResponseDto create(MachineRequestDto request);
    MachineResponseDto update(Long id, MachineRequestDto request);
    void delete(Long id);
}
