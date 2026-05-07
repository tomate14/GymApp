package com.fitexa.services.impl.dashboard;

import com.fitexa.dtos.DashboardChartDto;
import com.fitexa.dtos.DashboardChartPointDto;
import com.fitexa.repository.RoutineAssignmentExerciseRepository;
import com.fitexa.services.DashboardChartContext;
import com.fitexa.services.DashboardChartStrategy;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
        LocalDate effectiveToDate = context.getToDate().isBefore(LocalDate.now())
            ? context.getToDate()
            : LocalDate.now().minusDays(1);

        List<DashboardChartPointDto> points = effectiveToDate.isBefore(context.getFromDate())
            ? List.of()
            : routineAssignmentExerciseRepository.findTopMachinesUsageByCompletedRoutineDays(
                    context.getClientId(),
                    context.getFromDate(),
                    effectiveToDate
                ).stream()
                .map(item -> DashboardChartPointDto.builder()
                    .label(item.getMachineName())
                    .value(item.getUsageCount().doubleValue())
                    .build())
                .toList();

        return DashboardChartDto.builder()
            .key("top-machines")
            .title("Top 10 maquinas con mayor presion por unidad")
            .type("bar")
            .points(points)
            .build();
    }
}
