package com.fitexa.services;

import com.fitexa.dtos.MachineResponseDto;
import com.fitexa.dtos.DashboardPageResponseDto;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface MachineService {
    List<MachineResponseDto> findAll();
    DashboardPageResponseDto<MachineResponseDto> findPage(String name, boolean onlyInUse, int page, int size);
    MachineResponseDto findById(Long id);
    MachineResponseDto create(String name, MultipartFile photo);
    MachineResponseDto update(Long id, String name, MultipartFile photo);
    void delete(Long id);
}
