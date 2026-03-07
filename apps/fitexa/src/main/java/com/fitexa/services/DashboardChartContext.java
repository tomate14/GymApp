package com.fitexa.services;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DashboardChartContext {
    LocalDate fromDate;
    LocalDate toDate;
    LocalDate referenceDate;
    DashboardStudentsTrendMode studentsTrendMode;
}
