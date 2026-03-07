package com.fitexa.services.impl;

import com.fitexa.dtos.DashboardActivityRankingRowDto;
import com.fitexa.dtos.DashboardAlertDto;
import com.fitexa.dtos.DashboardChartDto;
import com.fitexa.dtos.DashboardClassRowDto;
import com.fitexa.dtos.DashboardKpiDto;
import com.fitexa.dtos.DashboardPageResponseDto;
import com.fitexa.dtos.DashboardPlanOverloadRowDto;
import com.fitexa.dtos.DashboardRenewalRowDto;
import com.fitexa.dtos.DashboardSummaryResponseDto;
import com.fitexa.entities.PaymentStatus;
import com.fitexa.entities.UserEntity;
import com.fitexa.entities.UserType;
import com.fitexa.repository.DashboardActivityRankingProjection;
import com.fitexa.repository.DashboardPlanOverloadProjection;
import com.fitexa.repository.GymClassRepository;
import com.fitexa.repository.RoutineAssignmentRepository;
import com.fitexa.repository.UserRepository;
import com.fitexa.services.DashboardChartContext;
import com.fitexa.services.DashboardChartStrategy;
import com.fitexa.services.DashboardService;
import com.fitexa.services.DashboardStudentsTrendMode;
import com.fitexa.utils.DashboardGymClassSpecifications;
import com.fitexa.utils.DashboardUserSpecifications;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.##");
    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,##0.00");

    private final UserRepository userRepository;
    private final GymClassRepository gymClassRepository;
    private final RoutineAssignmentRepository routineAssignmentRepository;
    private final List<DashboardChartStrategy> dashboardChartStrategies;

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryResponseDto getSummary(LocalDate fromDate, LocalDate toDate, String studentsTrendMode) {
        LocalDate[] range = resolveDateRange(fromDate, toDate);
        LocalDate effectiveFromDate = range[0];
        LocalDate effectiveToDate = range[1];
        DashboardStudentsTrendMode trendMode = DashboardStudentsTrendMode.fromValue(studentsTrendMode);

        long totalStudents = userRepository.countByUserType(UserType.ALUMNO);
        long newStudents = userRepository.countByUserTypeAndRegistrationDateBetween(UserType.ALUMNO, effectiveFromDate, effectiveToDate);
        long teachers = userRepository.countByUserTypeAndRegistrationDateBetween(UserType.PROFESOR, effectiveFromDate, effectiveToDate);
        long upToDate = userRepository.countByUserTypeAndPaymentStatusAndRegistrationDateBetween(
            UserType.ALUMNO,
            PaymentStatus.AL_DIA,
            effectiveFromDate,
            effectiveToDate
        );
        long renewalsInRange = userRepository.countByUserTypeAndRenewalDateBetween(UserType.ALUMNO, effectiveFromDate, effectiveToDate);
        long classesInRange = gymClassRepository.countByDateBetween(effectiveFromDate, effectiveToDate);
        double occupancy = defaultZero(gymClassRepository.averageOccupancyPercentage(effectiveFromDate, effectiveToDate));
        long activeRoutines = routineAssignmentRepository.countAssignmentsOverlappingRange(effectiveFromDate, effectiveToDate);
        BigDecimal estimatedRevenue = userRepository.sumEstimatedRevenueByUserTypeAndRegistrationDateBetween(
            UserType.ALUMNO,
            effectiveFromDate,
            effectiveToDate
        );

        List<DashboardKpiDto> kpis = List.of(
            DashboardKpiDto.builder().key("students-total").label("Alumnos totales").value(number(totalStudents)).hint("Base actual").build(),
            DashboardKpiDto.builder().key("students-new-period").label("Alumnos nuevos en el periodo").value(number(newStudents)).hint(dateRangeHint(effectiveFromDate, effectiveToDate)).build(),
            DashboardKpiDto.builder().key("teachers").label("Profesores (periodo)").value(number(teachers)).hint(dateRangeHint(effectiveFromDate, effectiveToDate)).build(),
            DashboardKpiDto.builder().key("payment-ok").label("Cuotas al dia").value(number(upToDate)).hint("Registrados en periodo").build(),
            DashboardKpiDto.builder().key("renewals-next").label("Vencimientos en periodo").value(number(renewalsInRange)).hint(dateRangeHint(effectiveFromDate, effectiveToDate)).build(),
            DashboardKpiDto.builder().key("classes-today").label("Clases en periodo").value(number(classesInRange)).hint(dateRangeHint(effectiveFromDate, effectiveToDate)).build(),
            DashboardKpiDto.builder().key("occupancy").label("Ocupacion promedio").value(percentage(occupancy)).hint("Dentro de periodo").build(),
            DashboardKpiDto.builder().key("active-routines").label("Rutinas en periodo").value(number(activeRoutines)).hint("Con interseccion de fechas").build(),
            DashboardKpiDto.builder().key("revenue").label("Ingreso estimado").value(currency(estimatedRevenue)).hint("Planes de alumnos del periodo").build()
        );

        long studentsWithoutPlan = userRepository.countByUserTypeAndPlanIsNullAndRegistrationDateBetween(
            UserType.ALUMNO,
            effectiveFromDate,
            effectiveToDate
        );
        long classesWithoutTeacher = gymClassRepository.countByTeacherIsNullAndDateBetween(effectiveFromDate, effectiveToDate);
        long classesAtCapacity = gymClassRepository.countAtOrAboveCapacityInRange(effectiveFromDate, effectiveToDate);
        long expiredRoutines = routineAssignmentRepository.countAssignmentsEndedInRange(effectiveFromDate, effectiveToDate);

        List<DashboardAlertDto> alerts = List.of(
            DashboardAlertDto.builder().key("students-without-plan").label("Alumnos sin plan").count(studentsWithoutPlan).severity("high").build(),
            DashboardAlertDto.builder().key("classes-without-teacher").label("Clases sin profesor").count(classesWithoutTeacher).severity("medium").build(),
            DashboardAlertDto.builder().key("classes-at-capacity").label("Clases al limite").count(classesAtCapacity).severity("medium").build(),
            DashboardAlertDto.builder().key("expired-routines").label("Rutinas vencidas").count(expiredRoutines).severity("low").build()
        );

        List<DashboardChartDto> charts = dashboardChartStrategies.stream()
            .sorted(Comparator.comparingInt(DashboardChartStrategy::order))
            .map(strategy -> strategy.build(
                DashboardChartContext.builder()
                    .fromDate(effectiveFromDate)
                    .toDate(effectiveToDate)
                    .referenceDate(effectiveToDate)
                    .studentsTrendMode(trendMode)
                    .build()
            ))
            .toList();

        return DashboardSummaryResponseDto.builder()
            .kpis(kpis)
            .alerts(alerts)
            .charts(charts)
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardPageResponseDto<DashboardRenewalRowDto> findRenewals(
        String search,
        Long planId,
        String paymentStatus,
        LocalDate fromDate,
        LocalDate toDate,
        LocalDate renewalFrom,
        LocalDate renewalTo,
        int page,
        int size
    ) {
        LocalDate[] globalRange = resolveDateRange(fromDate, toDate);
        LocalDate effectiveRenewalFrom = renewalFrom == null ? globalRange[0] : max(renewalFrom, globalRange[0]);
        LocalDate effectiveRenewalTo = renewalTo == null ? globalRange[1] : min(renewalTo, globalRange[1]);
        if (effectiveRenewalFrom.isAfter(effectiveRenewalTo)) {
            effectiveRenewalFrom = globalRange[0];
            effectiveRenewalTo = globalRange[1];
        }

        PaymentStatus parsedPaymentStatus = parsePaymentStatus(paymentStatus);
        Pageable pageable = PageRequest.of(
            sanitizePage(page),
            sanitizeSize(size),
            Sort.by(Sort.Direction.ASC, "renewalDate").and(Sort.by(Sort.Direction.ASC, "lastName"))
        );

        Page<UserEntity> resultPage = userRepository.findAll(
            DashboardUserSpecifications.buildRenewalsFilter(
                clean(search),
                planId,
                parsedPaymentStatus,
                effectiveRenewalFrom,
                effectiveRenewalTo
            ),
            pageable
        );

        Page<DashboardRenewalRowDto> dtoPage = resultPage.map(item -> DashboardRenewalRowDto.builder()
            .userId(item.getId())
            .studentName(item.getFirstName() + " " + item.getLastName())
            .dni(item.getDni())
            .planName(item.getPlan() == null ? "Sin plan" : item.getPlan().getName())
            .renewalDate(item.getRenewalDate())
            .paymentStatus(item.getPaymentStatus().getValue())
            .phone(item.getPhone())
            .build()
        );

        return toPageResponse(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardPageResponseDto<DashboardClassRowDto> findClasses(
        String search,
        Long activityId,
        Long teacherId,
        LocalDate fromDate,
        LocalDate toDate,
        LocalDate dateFrom,
        LocalDate dateTo,
        int page,
        int size
    ) {
        LocalDate[] globalRange = resolveDateRange(fromDate, toDate);
        LocalDate effectiveDateFrom = dateFrom == null ? globalRange[0] : max(dateFrom, globalRange[0]);
        LocalDate effectiveDateTo = dateTo == null ? globalRange[1] : min(dateTo, globalRange[1]);
        if (effectiveDateFrom.isAfter(effectiveDateTo)) {
            effectiveDateFrom = globalRange[0];
            effectiveDateTo = globalRange[1];
        }

        Pageable pageable = PageRequest.of(
            sanitizePage(page),
            sanitizeSize(size),
            Sort.by(Sort.Direction.ASC, "date").and(Sort.by(Sort.Direction.ASC, "time"))
        );

        Page<DashboardClassRowDto> dtoPage = gymClassRepository.findAll(
            DashboardGymClassSpecifications.buildClassScheduleFilter(clean(search), activityId, teacherId, effectiveDateFrom, effectiveDateTo),
            pageable
        ).map(item -> {
            double occupancy = item.getMaxCapacity() == null || item.getMaxCapacity() <= 0
                ? 0d
                : (item.getEnrolled() * 100.0) / item.getMaxCapacity();

            return DashboardClassRowDto.builder()
                .classId(item.getId())
                .className(item.getClassName())
                .activityName(item.getActivity() == null ? "Sin actividad" : item.getActivity().getName())
                .teacherName(item.getTeacher() == null ? "Sin profesor" : item.getTeacher().getFirstName() + " " + item.getTeacher().getLastName())
                .date(item.getDate())
                .time(item.getTime())
                .enrolled(item.getEnrolled())
                .maxCapacity(item.getMaxCapacity())
                .occupancyPercent(occupancy)
                .build();
        });

        return toPageResponse(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardPageResponseDto<DashboardActivityRankingRowDto> findActivityRanking(
        String search,
        LocalDate fromDate,
        LocalDate toDate,
        int page,
        int size
    ) {
        LocalDate[] range = resolveDateRange(fromDate, toDate);
        Pageable pageable = PageRequest.of(sanitizePage(page), sanitizeSize(size));

        Page<DashboardActivityRankingRowDto> dtoPage = gymClassRepository.findActivityRanking(
            range[0],
            range[1],
            clean(search),
            pageable
        ).map(this::toActivityRankingDto);

        return toPageResponse(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardPageResponseDto<DashboardPlanOverloadRowDto> findPlanOverloads(
        String search,
        LocalDate fromDate,
        LocalDate toDate,
        int page,
        int size
    ) {
        LocalDate[] globalRange = resolveDateRange(fromDate, toDate);
        Pageable pageable = PageRequest.of(sanitizePage(page), sanitizeSize(size));

        Page<DashboardPlanOverloadRowDto> dtoPage = routineAssignmentRepository.findPlanOverloadsInRange(
            globalRange[0],
            globalRange[1],
            clean(search),
            pageable
        ).map(this::toPlanOverloadDto);

        return toPageResponse(dtoPage);
    }

    private DashboardActivityRankingRowDto toActivityRankingDto(DashboardActivityRankingProjection projection) {
        return DashboardActivityRankingRowDto.builder()
            .activityId(projection.getActivityId())
            .activityName(projection.getActivityName())
            .classesCount(defaultZero(projection.getClassesCount()))
            .totalEnrolled(defaultZero(projection.getTotalEnrolled()))
            .averageOccupancyPercent(defaultZero(projection.getAverageOccupancyPercent()))
            .build();
    }

    private DashboardPlanOverloadRowDto toPlanOverloadDto(DashboardPlanOverloadProjection projection) {
        int assignedDays = projection.getAssignedDays() == null ? 0 : projection.getAssignedDays().intValue();
        int maxDays = projection.getMaxDaysPerWeek() == null ? 0 : projection.getMaxDaysPerWeek();
        return DashboardPlanOverloadRowDto.builder()
            .userId(projection.getUserId())
            .studentName(projection.getFirstName() + " " + projection.getLastName())
            .dni(projection.getDni())
            .planName(projection.getPlanName())
            .maxDaysPerWeek(maxDays)
            .assignedDays(assignedDays)
            .extraDays(Math.max(0, assignedDays - maxDays))
            .build();
    }

    private <T> DashboardPageResponseDto<T> toPageResponse(Page<T> page) {
        return DashboardPageResponseDto.<T>builder()
            .content(page.getContent())
            .page(page.getNumber())
            .size(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .build();
    }

    private LocalDate[] resolveDateRange(LocalDate fromDate, LocalDate toDate) {
        LocalDate today = LocalDate.now();
        LocalDate safeToDate = toDate == null ? today : toDate;
        LocalDate safeFromDate = fromDate == null ? safeToDate.minusMonths(1) : fromDate;

        if (safeFromDate.isAfter(safeToDate)) {
            return new LocalDate[] {safeToDate.minusMonths(1), safeToDate};
        }
        return new LocalDate[] {safeFromDate, safeToDate};
    }

    private LocalDate max(LocalDate first, LocalDate second) {
        return first.isAfter(second) ? first : second;
    }

    private LocalDate min(LocalDate first, LocalDate second) {
        return first.isBefore(second) ? first : second;
    }

    private PaymentStatus parsePaymentStatus(String paymentStatus) {
        if (paymentStatus == null || paymentStatus.isBlank()) {
            return null;
        }
        return PaymentStatus.fromValue(paymentStatus.trim());
    }

    private String clean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private int sanitizePage(int page) {
        return Math.max(0, page);
    }

    private int sanitizeSize(int size) {
        if (size <= 0) {
            return 8;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }

    private long defaultZero(Long value) {
        return value == null ? 0L : value;
    }

    private double defaultZero(Double value) {
        return value == null ? 0d : value;
    }

    private String number(long value) {
        return DECIMAL_FORMAT.format(value);
    }

    private String percentage(double value) {
        return DECIMAL_FORMAT.format(value) + "%";
    }

    private String currency(BigDecimal amount) {
        BigDecimal safeAmount = amount == null ? BigDecimal.ZERO : amount;
        return "$" + CURRENCY_FORMAT.format(safeAmount);
    }

    private String dateRangeHint(LocalDate fromDate, LocalDate toDate) {
        return fromDate + " a " + toDate;
    }
}
