package com.fitexa.services.impl.dashboard;

import com.fitexa.dtos.DashboardChartDto;
import com.fitexa.dtos.DashboardChartPointDto;
import com.fitexa.entities.PaymentStatus;
import com.fitexa.entities.UserType;
import com.fitexa.repository.UserRepository;
import com.fitexa.services.DashboardChartContext;
import com.fitexa.services.DashboardChartStrategy;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DashboardPaymentStatusChartStrategy implements DashboardChartStrategy {

    private final UserRepository userRepository;

    @Override
    public int order() {
        return 20;
    }

    @Override
    public DashboardChartDto build(DashboardChartContext context) {
        java.time.LocalDate referenceDate = java.time.LocalDate.now();
        Map<PaymentStatus, Long> countByStatus = new LinkedHashMap<>();
        Arrays.stream(PaymentStatus.values()).forEach(status -> countByStatus.put(status, 0L));
        userRepository.countUsersByPaymentStatusUpToDate(
            UserType.ALUMNO,
            context.getClientId(),
            referenceDate
        ).forEach(item ->
            countByStatus.put(PaymentStatus.valueOf(String.valueOf(item[0])), ((Number) item[1]).longValue())
        );

        List<DashboardChartPointDto> points = countByStatus.entrySet().stream()
            .map(entry -> DashboardChartPointDto.builder()
                .label(entry.getKey().getValue())
                .value(entry.getValue().doubleValue())
                .build())
            .toList();

        return DashboardChartDto.builder()
            .key("payment-status")
            .title("Estado de cuotas")
            .type("donut")
            .points(points)
            .build();
    }
}
