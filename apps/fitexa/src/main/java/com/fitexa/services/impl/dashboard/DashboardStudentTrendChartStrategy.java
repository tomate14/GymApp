package com.fitexa.services.impl.dashboard;

import com.fitexa.dtos.DashboardChartDto;
import com.fitexa.dtos.DashboardChartPointDto;
import com.fitexa.repository.UserRepository;
import com.fitexa.services.DashboardChartContext;
import com.fitexa.services.DashboardChartStrategy;
import com.fitexa.services.DashboardStudentsTrendMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DashboardStudentTrendChartStrategy implements DashboardChartStrategy {

    private final UserRepository userRepository;

    @Override
    public int order() {
        return 30;
    }

    @Override
    public DashboardChartDto build(DashboardChartContext context) {
        if (context.getStudentsTrendMode() == DashboardStudentsTrendMode.YEAR) {
            return buildYearTrend(context);
        }
        return buildMonthTrend(context);
    }

    private DashboardChartDto buildMonthTrend(DashboardChartContext context) {
        LocalDate toDate = context.getToDate();
        YearMonth end = YearMonth.from(toDate);
        YearMonth start = YearMonth.from(context.getFromDate());
        if (start.plusMonths(11).isBefore(end)) {
            start = end.minusMonths(11);
        }
        LocalDate queryFromDate = start.atDay(1);

        Map<String, Long> valuesByPeriod = new LinkedHashMap<>();
        YearMonth current = start;
        while (!current.isAfter(end)) {
            valuesByPeriod.put(current.toString(), 0L);
            current = current.plusMonths(1);
        }

        userRepository.countStudentRegistrationsByMonth(queryFromDate, toDate).forEach(item -> {
            String key = String.valueOf(item[0]);
            long total = ((Number) item[1]).longValue();
            if (valuesByPeriod.containsKey(key)) {
                valuesByPeriod.put(key, total);
            }
        });

        List<DashboardChartPointDto> points = new ArrayList<>();
        valuesByPeriod.forEach((period, total) ->
            points.add(DashboardChartPointDto.builder()
                .label(period)
                .value(total)
                .build())
        );

        return DashboardChartDto.builder()
            .key("students-trend")
            .title("Altas de alumnos por mes")
            .type("bar")
            .points(points)
            .build();
    }

    private DashboardChartDto buildYearTrend(DashboardChartContext context) {
        int endYear = context.getToDate().getYear();
        int startYear = endYear - 11;
        LocalDate queryFromDate = LocalDate.of(startYear, 1, 1);
        LocalDate queryToDate = context.getToDate();

        Map<String, Long> valuesByPeriod = new LinkedHashMap<>();
        for (int year = startYear; year <= endYear; year++) {
            valuesByPeriod.put(String.valueOf(year), 0L);
        }

        userRepository.countStudentRegistrationsByYear(queryFromDate, queryToDate).forEach(item -> {
            String key = String.valueOf(item[0]);
            long total = ((Number) item[1]).longValue();
            if (valuesByPeriod.containsKey(key)) {
                valuesByPeriod.put(key, total);
            }
        });

        List<DashboardChartPointDto> points = new ArrayList<>();
        valuesByPeriod.forEach((period, total) ->
            points.add(DashboardChartPointDto.builder()
                .label(period)
                .value(total)
                .build())
        );

        return DashboardChartDto.builder()
            .key("students-trend")
            .title("Altas de alumnos por ano")
            .type("bar")
            .points(points)
            .build();
    }
}
