package com.fitexa.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AuthChangePasswordRequestDto {

    @NotBlank
    private String currentPassword;

    @NotBlank
    @Size(min = 6, max = 120, message = "debe tener entre 6 y 120 caracteres")
    private String newPassword;
}
