package com.fitexa.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardChartPointDto {
    private String label;
    private double value;
}
