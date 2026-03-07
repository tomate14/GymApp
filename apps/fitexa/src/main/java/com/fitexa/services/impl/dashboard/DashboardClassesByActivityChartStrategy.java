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
public class DashboardClassesByActivityChartStrategy implements DashboardChartStrategy {

    private final GymClassRepository gymClassRepository;

    @Override
    public int order() {
        return 40;
    }

    @Override
    public DashboardChartDto build(DashboardChartContext context) {
        List<DashboardChartPointDto> points = gymClassRepository.countClassesByActivity(
                context.getFromDate(),
                context.getToDate()
            ).stream()
            .map(item -> DashboardChartPointDto.builder()
                .label(String.valueOf(item[0]))
                .value(((Number) item[1]).doubleValue())
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
