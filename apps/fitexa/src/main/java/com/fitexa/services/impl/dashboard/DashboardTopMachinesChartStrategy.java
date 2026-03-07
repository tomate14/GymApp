package com.fitexa.services.impl.dashboard;

import com.fitexa.dtos.DashboardChartDto;
import com.fitexa.dtos.DashboardChartPointDto;
import com.fitexa.repository.RoutineAssignmentExerciseRepository;
import com.fitexa.services.DashboardChartContext;
import com.fitexa.services.DashboardChartStrategy;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DashboardTopMachinesChartStrategy implements DashboardChartStrategy {

    private final RoutineAssignmentExerciseRepository routineAssignmentExerciseRepository;

    @Override
    public int order() {
        return 80;
    }

    @Override
    public DashboardChartDto build(DashboardChartContext context) {
        List<DashboardChartPointDto> points = routineAssignmentExerciseRepository.findTopMachinesUsageInRange(
                context.getFromDate(),
                context.getToDate(),
                PageRequest.of(0, 10)
            ).stream()
            .map(item -> DashboardChartPointDto.builder()
                .label(item.getMachineName())
                .value(item.getUsageCount().doubleValue())
                .build())
            .toList();

        return DashboardChartDto.builder()
            .key("top-machines")
            .title("Top 10 maquinas mas usadas")
            .type("bar")
            .points(points)
            .build();
    }
}
