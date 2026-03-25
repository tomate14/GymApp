package com.fitexa.services;

import com.fitexa.dtos.DashboardActivityRankingRowDto;
import com.fitexa.dtos.DashboardChartDto;
import com.fitexa.dtos.DashboardClassRowDto;
import com.fitexa.dtos.DashboardPageResponseDto;
import com.fitexa.dtos.DashboardPlanOverloadRowDto;
import com.fitexa.dtos.DashboardRenewalRowDto;
import com.fitexa.dtos.DashboardSummaryResponseDto;
import java.time.LocalDate;

public interface DashboardService {
    DashboardSummaryResponseDto getSummary(LocalDate fromDate, LocalDate toDate, String studentsTrendMode);

    DashboardChartDto getStudentsTrendChart(LocalDate fromDate, LocalDate toDate, String studentsTrendMode);

    DashboardPageResponseDto<DashboardRenewalRowDto> findRenewals(
        String search,
        Long planId,
        String paymentStatus,
        LocalDate fromDate,
        LocalDate toDate,
        LocalDate renewalFrom,
        LocalDate renewalTo,
        int page,
        int size
    );

    DashboardPageResponseDto<DashboardClassRowDto> findClasses(
        String search,
        Long activityId,
        Long teacherId,
        LocalDate fromDate,
        LocalDate toDate,
        int page,
        int size
    );

    DashboardPageResponseDto<DashboardActivityRankingRowDto> findActivityRanking(
        String search,
        LocalDate fromDate,
        LocalDate toDate,
        int page,
        int size
    );

    DashboardPageResponseDto<DashboardPlanOverloadRowDto> findPlanOverloads(
        String search,
        LocalDate fromDate,
        LocalDate toDate,
        int page,
        int size
    );
}
