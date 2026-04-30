package com.fitexa.dtos;

import com.fitexa.entities.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class UserRequestDto {
    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    private String dni;

    @NotBlank
    private String address;

    @NotBlank
    private String phone;

    @Email
    @NotBlank
    private String email;

    @Size(min = 6, max = 120, message = "debe tener entre 6 y 120 caracteres")
    private String password;

    @NotNull
    private LocalDate birthDate;

    private UserType userType;

    private Long roleId;

    private Long planId;

    private List<Long> activityIds;

    private LocalDate registrationDate;
}
