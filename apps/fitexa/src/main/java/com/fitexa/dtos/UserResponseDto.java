package com.fitexa.dtos;

import com.fitexa.entities.PaymentStatus;
import com.fitexa.entities.UserType;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponseDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String dni;
    private String address;
    private String phone;
    private String email;
    private LocalDate birthDate;
    private UserType userType;
    private Long planId;
    private String planName;
    private BigDecimal planPrice;
    private LocalDate registrationDate;
    private LocalDate renewalDate;
    private PaymentStatus paymentStatus;
}
