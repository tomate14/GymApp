package com.fitexa.dtos.mobile;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MobilePersonalRecordResponseDto {
    private Long exerciseId;
    private String exerciseName;
    private String activityName;
    private Double maxWeight;
    private LocalDate recordedAt;
}
