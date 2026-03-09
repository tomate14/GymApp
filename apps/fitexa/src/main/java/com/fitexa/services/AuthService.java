package com.fitexa.services;

import com.fitexa.dtos.AuthChangePasswordRequestDto;
import com.fitexa.dtos.AuthChangePasswordResponseDto;
import com.fitexa.dtos.AuthLoginRequestDto;
import com.fitexa.dtos.AuthLoginResponseDto;

public interface AuthService {
    AuthLoginResponseDto login(AuthLoginRequestDto request);
    AuthChangePasswordResponseDto changePassword(AuthChangePasswordRequestDto request);
}
