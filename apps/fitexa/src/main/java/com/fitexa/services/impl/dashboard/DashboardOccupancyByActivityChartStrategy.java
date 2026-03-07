package com.fitexa.services.impl.dashboard;

import com.fitexa.dtos.DashboardChartDto;
import com.fitexa.dtos.DashboardChartPointDto;
import com.fitexa.repository.GymClassRepository;
import com.fitexa.services.DashboardChartContext;
import com.fitexa.services.DashboardChartStrategy;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DashboardOccupancyByActivityChartStrategy implements DashboardChartStrategy {

    private final GymClassRepository gymClassRepository;

    @Override
    public int order() {
        return 50;
    }

    @Override
    public DashboardChartDto build(DashboardChartContext context) {
        List<DashboardChartPointDto> points = gymClassRepository.averageOccupancyByActivity(
                context.getFromDate(),
                context.getToDate()
            ).stream()
            .map(item -> DashboardChartPointDto.builder()
                .label(String.valueOf(item[0]))
                .value(item[1] == null ? 0d : ((Number) item[1]).doubleValue())
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
