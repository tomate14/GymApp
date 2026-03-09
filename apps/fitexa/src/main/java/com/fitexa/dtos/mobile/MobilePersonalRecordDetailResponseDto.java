package com.fitexa.dtos.mobile;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MobilePersonalRecordDetailResponseDto {
    private Long exerciseId;
    private String exerciseName;
    private String activityName;
    private Double maxWeight;
    private LocalDate recordedAt;
    private List<MobilePersonalRecordHistoryPointResponseDto> history;
}
