package com.fitexa.services;

import com.fitexa.dtos.UserRequestDto;
import com.fitexa.dtos.UserRoleAssignmentRequestDto;
import com.fitexa.dtos.UserResponseDto;
import com.fitexa.dtos.DashboardPageResponseDto;
import java.time.LocalDate;
import java.util.List;

public interface UserService {
    List<UserResponseDto> findAll();
    DashboardPageResponseDto<UserResponseDto> findPage(
        String type,
        Long roleId,
        String name,
        String dni,
        String paymentStatus,
        LocalDate registrationFrom,
        LocalDate registrationTo,
        LocalDate renewalFrom,
        LocalDate renewalTo,
        int page,
        int size
    );
    List<UserResponseDto> findStudents();
    List<UserResponseDto> findTeachers();
    UserResponseDto findById(Long id);
    UserResponseDto create(UserRequestDto request);
    UserResponseDto update(Long id, UserRequestDto request);
    UserResponseDto assignRole(Long id, UserRoleAssignmentRequestDto request);
    UserResponseDto renewQuota(Long id);
    void delete(Long id);
}
