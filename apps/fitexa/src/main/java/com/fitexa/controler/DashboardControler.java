package com.fitexa.controler;

import com.fitexa.dtos.DashboardActivityRankingRowDto;
import com.fitexa.dtos.DashboardClassRowDto;
import com.fitexa.dtos.DashboardPageResponseDto;
import com.fitexa.dtos.DashboardPlanOverloadRowDto;
import com.fitexa.dtos.DashboardRenewalRowDto;
import com.fitexa.dtos.DashboardSummaryResponseDto;
import com.fitexa.services.DashboardService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardControler {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public DashboardSummaryResponseDto getSummary(
        @RequestParam(name = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @RequestParam(name = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
        @RequestParam(name = "studentsTrendMode", required = false) String studentsTrendMode
    ) {
        return dashboardService.getSummary(fromDate, toDate, studentsTrendMode);
    }

    @GetMapping("/renewals")
    public DashboardPageResponseDto<DashboardRenewalRowDto> findRenewals(
        @RequestParam(name = "search", required = false) String search,
        @RequestParam(name = "planId", required = false) Long planId,
        @RequestParam(name = "paymentStatus", required = false) String paymentStatus,
        @RequestParam(name = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @RequestParam(name = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
        @RequestParam(name = "renewalFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate renewalFrom,
        @RequestParam(name = "renewalTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate renewalTo,
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "8") int size
    ) {
        return dashboardService.findRenewals(search, planId, paymentStatus, fromDate, toDate, renewalFrom, renewalTo, page, size);
    }

    @GetMapping("/classes")
    public DashboardPageResponseDto<DashboardClassRowDto> findClasses(
        @RequestParam(name = "search", required = false) String search,
        @RequestParam(name = "activityId", required = false) Long activityId,
        @RequestParam(name = "teacherId", required = false) Long teacherId,
        @RequestParam(name = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @RequestParam(name = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
        @RequestParam(name = "dateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
        @RequestParam(name = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "8") int size
    ) {
        return dashboardService.findClasses(search, activityId, teacherId, fromDate, toDate, dateFrom, dateTo, page, size);
    }

    @GetMapping("/activity-ranking")
    public DashboardPageResponseDto<DashboardActivityRankingRowDto> findActivityRanking(
        @RequestParam(name = "search", required = false) String search,
        @RequestParam(name = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @RequestParam(name = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "8") int size
    ) {
        return dashboardService.findActivityRanking(search, fromDate, toDate, page, size);
    }

    @GetMapping("/plan-overloads")
    public DashboardPageResponseDto<DashboardPlanOverloadRowDto> findPlanOverloads(
        @RequestParam(name = "search", required = false) String search,
        @RequestParam(name = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @RequestParam(name = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "8") int size
    ) {
        return dashboardService.findPlanOverloads(search, fromDate, toDate, page, size);
    }
}
