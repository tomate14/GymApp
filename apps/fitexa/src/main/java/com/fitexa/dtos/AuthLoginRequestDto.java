package com.fitexa.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthLoginRequestDto {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;
}
