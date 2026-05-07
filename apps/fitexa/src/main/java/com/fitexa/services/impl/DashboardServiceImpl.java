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
import com.fitexa.repository.DashboardPlanOverloadProjection;
import com.fitexa.repository.RoutineAssignmentRepository;
import com.fitexa.repository.UserRepository;
import com.fitexa.repository.UserPaymentMovementRepository;
import com.fitexa.services.AuthenticatedUserService;
import com.fitexa.services.DashboardChartContext;
import com.fitexa.services.DashboardChartStrategy;
import com.fitexa.services.DashboardService;
import com.fitexa.services.DashboardStudentsTrendMode;
import com.fitexa.services.impl.dashboard.DashboardClassMetricsService;
import com.fitexa.services.impl.dashboard.DashboardClassOccurrenceMetric;
import com.fitexa.services.impl.dashboard.DashboardStudentTrendChartStrategy;
import com.fitexa.utils.DashboardGymClassSpecifications;
import com.fitexa.utils.DashboardUserSpecifications;
import com.fitexa.utils.PaymentStatusUtils;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private static final String RENEWAL_DETAIL = "Renovacion de cuota";

    private final UserRepository userRepository;
    private final UserPaymentMovementRepository userPaymentMovementRepository;
    private final RoutineAssignmentRepository routineAssignmentRepository;
    private final DashboardClassMetricsService dashboardClassMetricsService;
    private final DashboardStudentTrendChartStrategy dashboardStudentTrendChartStrategy;
    private final List<DashboardChartStrategy> dashboardChartStrategies;
    private final AuthenticatedUserService authenticatedUserService;

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryResponseDto getSummary(LocalDate fromDate, LocalDate toDate, String studentsTrendMode) {
        LocalDate[] range = resolveDateRange(fromDate, toDate);
        LocalDate effectiveFromDate = range[0];
        LocalDate effectiveToDate = range[1];
        DashboardStudentsTrendMode trendMode = DashboardStudentsTrendMode.fromValue(studentsTrendMode);
        Long clientId = currentClientId();

        long totalStudents = userRepository.countByUserTypeAndClientId(UserType.ALUMNO, clientId);
        long newStudents = userRepository.countByUserTypeAndClientIdAndRegistrationDateBetween(UserType.ALUMNO, clientId, effectiveFromDate, effectiveToDate);
        List<DashboardClassOccurrenceMetric> classOccurrences = dashboardClassMetricsService.listOccurrences(effectiveFromDate, effectiveToDate);
        long teachers = classOccurrences.stream()
            .filter(DashboardClassOccurrenceMetric::hasTeacherAssigned)
            .map(DashboardClassOccurrenceMetric::getTeacherId)
            .distinct()
            .count();
        long upToDate = userPaymentMovementRepository.countDistinctUsersByDetailAndPaymentStatusAndPaidAtBetween(
            clientId,
            RENEWAL_DETAIL,
            true,
            LocalDate.now(),
            effectiveFromDate.atStartOfDay(),
            effectiveToDate.atTime(LocalTime.MAX)
        );
        long renewalsInRange = userRepository.countByUserTypeAndClientIdAndRenewalDateBetween(UserType.ALUMNO, clientId, effectiveFromDate, effectiveToDate);
        long classesInRange = classOccurrences.size();
        double occupancy = averageOccupancy(classOccurrences);
        long activeRoutines = routineAssignmentRepository.countAssignmentsOverlappingRange(clientId, effectiveFromDate, effectiveToDate);
        BigDecimal estimatedRevenue = userPaymentMovementRepository.sumAmountByDetailAndPaidAtBetween(
            clientId,
            RENEWAL_DETAIL,
            effectiveFromDate.atStartOfDay(),
            effectiveToDate.atTime(LocalTime.MAX)
        );

        List<DashboardKpiDto> kpis = List.of(
            DashboardKpiDto.builder().key("students-total").label("Alumnos totales").value(number(totalStudents)).hint("Base actual").build(),
            DashboardKpiDto.builder().key("students-new-period").label("Altas de alumnos").value(number(newStudents)).hint(dateRangeHint(effectiveFromDate, effectiveToDate)).build(),
            DashboardKpiDto.builder().key("teachers").label("Profesores con clases").value(number(teachers)).hint("Con clases dictadas en el periodo").build(),
            DashboardKpiDto.builder().key("payment-ok").label("Cuotas al dia").value(number(upToDate)).hint("Renovaciones cobradas en el periodo").build(),
            DashboardKpiDto.builder().key("renewals-next").label("Vencimientos en periodo").value(number(renewalsInRange)).hint(dateRangeHint(effectiveFromDate, effectiveToDate)).build(),
            DashboardKpiDto.builder().key("classes-today").label("Clases en periodo").value(number(classesInRange)).hint(dateRangeHint(effectiveFromDate, effectiveToDate)).build(),
            DashboardKpiDto.builder().key("occupancy").label("Ocupacion promedio").value(percentage(occupancy)).hint("Asistencia real sobre cupo").build(),
            DashboardKpiDto.builder().key("active-routines").label("Rutinas en periodo").value(number(activeRoutines)).hint("Con interseccion de fechas").build(),
            DashboardKpiDto.builder().key("revenue").label("Ingresos por renovaciones").value(currency(estimatedRevenue)).hint("Cobros registrados en el periodo").build()
        );

        long studentsWithoutPlan = userRepository.countByUserTypeAndClientIdAndPlanIsNullAndRegistrationDateBetween(
            UserType.ALUMNO,
            clientId,
            effectiveFromDate,
            effectiveToDate
        );
        long classesWithoutTeacher = classOccurrences.stream().filter(item -> !item.hasTeacherAssigned()).count();
        long classesAtCapacity = classOccurrences.stream().filter(DashboardClassOccurrenceMetric::isAtCapacity).count();
        long expiredRoutines = routineAssignmentRepository.countAssignmentsEndedInRange(clientId, effectiveFromDate, effectiveToDate);

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
                    .clientId(clientId)
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
    public DashboardChartDto getStudentsTrendChart(LocalDate fromDate, LocalDate toDate, String studentsTrendMode) {
        LocalDate[] range = resolveDateRange(fromDate, toDate);
        DashboardStudentsTrendMode trendMode = DashboardStudentsTrendMode.fromValue(studentsTrendMode);
        return dashboardStudentTrendChartStrategy.build(
            DashboardChartContext.builder()
                .clientId(currentClientId())
                .fromDate(range[0])
                .toDate(range[1])
                .referenceDate(range[1])
                .studentsTrendMode(trendMode)
                .build()
        );
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
                currentClientId(),
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
            .paymentStatus(PaymentStatusUtils.fromRenewalDate(item.getRenewalDate()).getValue())
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
        int page,
        int size
    ) {
        LocalDate[] range = resolveDateRange(fromDate, toDate);
        LocalDate effectiveFrom = range[0];
        LocalDate effectiveTo = range[1];

        List<DashboardClassRowDto> scheduleRows = dashboardClassMetricsService
            .listOccurrences(
                effectiveFrom,
                effectiveTo,
                DashboardGymClassSpecifications.buildClassScheduleFilter(currentClientId(), clean(search), activityId, teacherId)
            )
            .stream()
            .map(this::toClassRowDto)
            .sorted(
                Comparator
                    .comparing(DashboardClassRowDto::getDate)
                    .thenComparing(row -> Boolean.TRUE.equals(row.getAllDay()) ? 0 : 1)
                    .thenComparing(DashboardClassRowDto::getStartTime, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(DashboardClassRowDto::getClassName)
            )
            .toList();

        int safePage = sanitizePage(page);
        int safeSize = sanitizeSize(size);
        int fromIndex = Math.min(safePage * safeSize, scheduleRows.size());
        int toIndex = Math.min(fromIndex + safeSize, scheduleRows.size());
        List<DashboardClassRowDto> content = scheduleRows.subList(fromIndex, toIndex);
        int totalPages = scheduleRows.isEmpty() ? 1 : (int) Math.ceil(scheduleRows.size() / (double) safeSize);

        return DashboardPageResponseDto.<DashboardClassRowDto>builder()
            .content(content)
            .page(safePage)
            .size(safeSize)
            .totalElements(scheduleRows.size())
            .totalPages(totalPages)
            .build();
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
        String safeSearch = clean(search);
        Map<String, ActivityRankingAccumulator> aggregations = new HashMap<>();

        for (DashboardClassOccurrenceMetric occurrence : dashboardClassMetricsService.listOccurrences(range[0], range[1])) {
            String activityName = occurrence.getActivityName();
            if (safeSearch != null && !activityName.toLowerCase().contains(safeSearch.toLowerCase())) {
                continue;
            }

            String key = (occurrence.getActivityId() == null ? "null" : occurrence.getActivityId().toString()) + "|" + activityName;
            ActivityRankingAccumulator accumulator = aggregations.computeIfAbsent(
                key,
                ignored -> new ActivityRankingAccumulator(occurrence.getActivityId(), activityName)
            );
            accumulator.add(occurrence);
        }

        List<DashboardActivityRankingRowDto> rows = aggregations.values()
            .stream()
            .map(ActivityRankingAccumulator::toDto)
            .sorted(
                Comparator
                    .comparingLong(DashboardActivityRankingRowDto::getClassesCount)
                    .thenComparingLong(DashboardActivityRankingRowDto::getTotalEnrolled)
                    .reversed()
            )
            .toList();

        int safePage = sanitizePage(page);
        int safeSize = sanitizeSize(size);
        int fromIndex = Math.min(safePage * safeSize, rows.size());
        int toIndex = Math.min(fromIndex + safeSize, rows.size());
        List<DashboardActivityRankingRowDto> content = rows.subList(fromIndex, toIndex);
        int totalPages = rows.isEmpty() ? 1 : (int) Math.ceil(rows.size() / (double) safeSize);

        return DashboardPageResponseDto.<DashboardActivityRankingRowDto>builder()
            .content(content)
            .page(safePage)
            .size(safeSize)
            .totalElements(rows.size())
            .totalPages(totalPages)
            .build();
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
            currentClientId(),
            globalRange[0],
            globalRange[1],
            clean(search),
            pageable
        ).map(this::toPlanOverloadDto);

        return toPageResponse(dtoPage);
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

    private DashboardClassRowDto toClassRowDto(DashboardClassOccurrenceMetric item) {
        return DashboardClassRowDto.builder()
            .classId(item.getGymClass().getId())
            .className(item.getGymClass().getClassName())
            .activityName(item.getActivityName())
            .teacherName(item.getTeacherName())
            .date(item.getDate())
            .weekDay(item.getDate().getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.forLanguageTag("es-AR")))
            .allDay(item.isAllDay())
            .startTime(item.getStartTime())
            .endTime(item.getEndTime())
            .enrolled(item.getEnrolled())
            .maxCapacity(item.getMaxCapacity())
            .occupancyPercent(item.getOccupancyPercent())
            .build();
    }

    private double averageOccupancy(List<DashboardClassOccurrenceMetric> occurrences) {
        return occurrences.stream()
            .filter(item -> item.getMaxCapacity() != null && item.getMaxCapacity() > 0)
            .mapToDouble(DashboardClassOccurrenceMetric::getOccupancyPercent)
            .average()
            .orElse(0d);
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

    private Long currentClientId() {
        return authenticatedUserService.getCurrentClientId();
    }

    private static final class ActivityRankingAccumulator {
        private final Long activityId;
        private final String activityName;
        private long classesCount = 0L;
        private long totalEnrolled = 0L;
        private double occupancySum = 0d;

        private ActivityRankingAccumulator(Long activityId, String activityName) {
            this.activityId = activityId;
            this.activityName = activityName;
        }

        private void add(DashboardClassOccurrenceMetric occurrence) {
            classesCount++;
            totalEnrolled += occurrence.getEnrolled();
            if (occurrence.getMaxCapacity() != null && occurrence.getMaxCapacity() > 0) {
                occupancySum += occurrence.getOccupancyPercent();
            }
        }

        private DashboardActivityRankingRowDto toDto() {
            double average = classesCount == 0 ? 0d : occupancySum / classesCount;
            return DashboardActivityRankingRowDto.builder()
                .activityId(activityId)
                .activityName(activityName)
                .classesCount(classesCount)
                .totalEnrolled(totalEnrolled)
                .averageOccupancyPercent(average)
                .build();
        }
    }
}
