package com.fitexa.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardKpiDto {
    private String key;
    private String label;
    private String value;
    private String hint;
}
