package com.fitexa.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthChangePasswordResponseDto {
    private String message;
}
