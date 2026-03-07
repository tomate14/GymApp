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
public class DashboardTopExercisesChartStrategy implements DashboardChartStrategy {

    private final RoutineAssignmentExerciseRepository routineAssignmentExerciseRepository;

    @Override
    public int order() {
        return 70;
    }

    @Override
    public DashboardChartDto build(DashboardChartContext context) {
        List<DashboardChartPointDto> points = routineAssignmentExerciseRepository.findTopExercisesUsageInRange(
                context.getFromDate(),
                context.getToDate(),
                PageRequest.of(0, 10)
            ).stream()
            .map(item -> DashboardChartPointDto.builder()
                .label(item.getExerciseName())
                .value(item.getUsageCount().doubleValue())
                .build())
            .toList();

        return DashboardChartDto.builder()
            .key("top-exercises")
            .title("Top 10 ejercicios mas usados")
            .type("bar")
            .points(points)
            .build();
    }
}
