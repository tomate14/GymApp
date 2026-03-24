package com.fitexa.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GymClassAttendanceUpdateItemDto {
    @NotNull
    private Long subscriptionId;

    @NotNull
    private Boolean attendancePresent;
}
