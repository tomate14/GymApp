package com.fitexa.services.impl.dashboard;

import com.fitexa.dtos.DashboardChartDto;
import com.fitexa.dtos.DashboardChartPointDto;
import com.fitexa.repository.GymClassRepository;
import com.fitexa.services.DashboardChartContext;
import com.fitexa.services.DashboardChartStrategy;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DashboardClassesByActivityChartStrategy implements DashboardChartStrategy {

    private final DashboardClassMetricsService dashboardClassMetricsService;
    private final GymClassRepository gymClassRepository;

    @Override
    public int order() {
        return 40;
    }

    @Override
    public DashboardChartDto build(DashboardChartContext context) {
        LocalDate referenceDate = LocalDate.now();
        LocalDate fromDate = gymClassRepository.findEarliestClassDate();
        if (fromDate == null || fromDate.isAfter(referenceDate)) {
            fromDate = referenceDate;
        }

        List<DashboardChartPointDto> points = dashboardClassMetricsService.listOccurrences(
                fromDate,
                referenceDate
            ).stream()
            .collect(java.util.stream.Collectors.groupingBy(
                DashboardClassOccurrenceMetric::getActivityName,
                java.util.stream.Collectors.counting()
            ))
            .entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .map(item -> DashboardChartPointDto.builder()
                .label(item.getKey())
                .value(item.getValue().doubleValue())
                .build())
            .toList();

        return DashboardChartDto.builder()
            .key("classes-by-activity")
            .title("Clases por actividad")
            .type("bar")
            .points(points)
            .build();
    }
}
