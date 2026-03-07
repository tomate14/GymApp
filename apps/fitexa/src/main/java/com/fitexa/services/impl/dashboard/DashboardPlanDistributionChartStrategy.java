package com.fitexa.services.impl.dashboard;

import com.fitexa.dtos.DashboardChartDto;
import com.fitexa.dtos.DashboardChartPointDto;
import com.fitexa.entities.UserType;
import com.fitexa.repository.UserRepository;
import com.fitexa.services.DashboardChartContext;
import com.fitexa.services.DashboardChartStrategy;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DashboardPlanDistributionChartStrategy implements DashboardChartStrategy {

    private final UserRepository userRepository;

    @Override
    public int order() {
        return 10;
    }

    @Override
    public DashboardChartDto build(DashboardChartContext context) {
        List<DashboardChartPointDto> points = userRepository.countUsersByPlanInRegistrationRange(
                UserType.ALUMNO,
                context.getFromDate(),
                context.getToDate()
            ).stream()
            .map(item -> DashboardChartPointDto.builder()
                .label(String.valueOf(item[1]))
                .value(((Number) item[2]).doubleValue())
                .build())
            .toList();

        return DashboardChartDto.builder()
            .key("students-by-plan")
            .title("Alumnos por plan")
            .type("donut")
            .points(points)
            .build();
    }
}
