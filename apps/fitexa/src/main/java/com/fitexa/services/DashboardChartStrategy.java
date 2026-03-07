package com.fitexa.services;

import com.fitexa.dtos.DashboardChartDto;

public interface DashboardChartStrategy {
    int order();
    DashboardChartDto build(DashboardChartContext context);
}
