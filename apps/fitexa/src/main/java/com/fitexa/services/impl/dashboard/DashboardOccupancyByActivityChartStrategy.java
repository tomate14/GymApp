package com.fitexa.services.impl.dashboard;

import com.fitexa.dtos.DashboardChartDto;
import com.fitexa.dtos.DashboardChartPointDto;
import com.fitexa.services.DashboardChartContext;
import com.fitexa.services.DashboardChartStrategy;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DashboardOccupancyByActivityChartStrategy implements DashboardChartStrategy {

    private final DashboardClassMetricsService dashboardClassMetricsService;

    @Override
    public int order() {
        return 50;
    }

    @Override
    public DashboardChartDto build(DashboardChartContext context) {
        List<DashboardChartPointDto> points = dashboardClassMetricsService.listOccurrences(
                context.getFromDate(),
                context.getToDate()
            ).stream()
            .filter(item -> item.getMaxCapacity() != null && item.getMaxCapacity() > 0)
            .collect(java.util.stream.Collectors.groupingBy(
                DashboardClassOccurrenceMetric::getActivityName,
                java.util.stream.Collectors.averagingDouble(DashboardClassOccurrenceMetric::getOccupancyPercent)
            ))
            .entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .map(item -> DashboardChartPointDto.builder()
                .label(item.getKey())
                .value(item.getValue())
                .build())
            .toList();

        return DashboardChartDto.builder()
            .key("occupancy-by-activity")
            .title("Ocupacion promedio por actividad (%)")
            .type("bar")
            .points(points)
            .build();
    }
}
