package com.fitexa.services;

import com.fitexa.dtos.UserRequestDto;
import com.fitexa.dtos.UserResponseDto;
import java.util.List;

public interface UserService {
    List<UserResponseDto> findAll();
    List<UserResponseDto> findStudents();
    List<UserResponseDto> findTeachers();
    UserResponseDto findById(Long id);
    UserResponseDto create(UserRequestDto request);
    UserResponseDto update(Long id, UserRequestDto request);
    UserResponseDto renewQuota(Long id);
    void delete(Long id);
}
