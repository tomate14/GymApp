package com.fitexa.dtos.mobile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MobilePaymentMovementResponseDto {
    private Long id;
    private String planName;
    private BigDecimal amount;
    private String detail;
    private LocalDateTime paidAt;
}
