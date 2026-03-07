package com.fitexa.dtos;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardChartDto {
    private String key;
    private String title;
    private String type;
    private List<DashboardChartPointDto> points;
}
