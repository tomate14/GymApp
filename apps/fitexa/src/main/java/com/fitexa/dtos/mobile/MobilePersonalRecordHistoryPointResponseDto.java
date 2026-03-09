package com.fitexa.dtos.mobile;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MobilePersonalRecordHistoryPointResponseDto {
    private Long id;
    private Double weight;
    private LocalDate recordedAt;
}
