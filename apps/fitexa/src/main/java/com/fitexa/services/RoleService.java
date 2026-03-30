package com.fitexa.services;

import com.fitexa.dtos.RoleMenuItemResponseDto;
import com.fitexa.dtos.RoleRequestDto;
import com.fitexa.dtos.RoleResponseDto;
import java.util.List;

public interface RoleService {
    List<RoleResponseDto> findAll();
    RoleResponseDto findById(Long id);
    RoleResponseDto create(RoleRequestDto request);
    RoleResponseDto update(Long id, RoleRequestDto request);
    void delete(Long id);
    List<RoleMenuItemResponseDto> findAvailableMenuItems();
}
