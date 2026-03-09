package com.fitexa.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthLoginResponseDto {
    private String token;
    private String tokenType;
    private long expiresInSeconds;
    private Long userId;
    private String fullName;
    private String email;
    private String userType;
    private boolean mustChangePassword;
}
