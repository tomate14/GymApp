package com.fitexa.services.impl.dashboard;

import com.fitexa.dtos.DashboardChartDto;
import com.fitexa.dtos.DashboardChartPointDto;
import com.fitexa.repository.GymClassSubscriptionRepository;
import com.fitexa.services.DashboardChartContext;
import com.fitexa.services.DashboardChartStrategy;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DashboardStudentsByTeacherChartStrategy implements DashboardChartStrategy {

    private final GymClassSubscriptionRepository gymClassSubscriptionRepository;

    @Override
    public int order() {
        return 60;
    }

    @Override
    public DashboardChartDto build(DashboardChartContext context) {
        List<DashboardChartPointDto> points = gymClassSubscriptionRepository.countStudentsByTeacherInRange(
                context.getFromDate(),
                context.getToDate()
            ).stream()
            .map(item -> DashboardChartPointDto.builder()
                .label(item.getTeacherName())
                .value(item.getStudentsCount().doubleValue())
                .build())
            .toList();

        return DashboardChartDto.builder()
            .key("students-by-teacher")
            .title("Alumnos por profesor")
            .type("bar-race")
            .points(points)
            .build();
    }
}
