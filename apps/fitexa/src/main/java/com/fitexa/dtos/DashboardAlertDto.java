package com.fitexa.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardAlertDto {
    private String key;
    private String label;
    private long count;
    private String severity;
}
