package com.fitexa.dtos;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardRenewalRowDto {
    private Long userId;
    private String studentName;
    private String dni;
    private String planName;
    private LocalDate renewalDate;
    private String paymentStatus;
    private String phone;
}
