package com.fitexa.dtos;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardSummaryResponseDto {
    private List<DashboardKpiDto> kpis;
    private List<DashboardAlertDto> alerts;
    private List<DashboardChartDto> charts;
}
