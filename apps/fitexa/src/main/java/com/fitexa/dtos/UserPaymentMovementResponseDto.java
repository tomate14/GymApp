package com.fitexa.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserPaymentMovementResponseDto {
    private Long id;
    private Long userId;
    private String studentName;
    private Long planId;
    private String planName;
    private BigDecimal amount;
    private String detail;
    private LocalDateTime paidAt;
}
