package com.fitexa.dtos;

import com.fitexa.entities.PaymentStatus;
import com.fitexa.entities.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
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

    @NotNull
    private LocalDate birthDate;

    @NotNull
    private UserType userType;

    private Long planId;

    private LocalDate registrationDate;

    @NotNull
    private PaymentStatus paymentStatus;
}
