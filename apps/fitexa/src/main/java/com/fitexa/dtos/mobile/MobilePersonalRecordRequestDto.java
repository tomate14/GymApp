package com.fitexa.dtos.mobile;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import lombok.Data;

@Data
public class MobilePersonalRecordRequestDto {
    @NotNull
    @Positive
    private Double maxWeight;

    @NotNull
    private LocalDate recordedAt;
}
