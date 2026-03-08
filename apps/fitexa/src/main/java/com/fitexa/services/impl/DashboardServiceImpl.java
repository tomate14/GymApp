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
import com.fitexa.entities.GymClassEntity;
import com.fitexa.entities.PaymentStatus;
import com.fitexa.entities.UserEntity;
import com.fitexa.entities.UserType;
import com.fitexa.entities.WeekDay;
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
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        int page,
        int size
    ) {
        LocalDate[] range = resolveDateRange(fromDate, toDate);
        LocalDate effectiveFrom = range[0];
        LocalDate effectiveTo = range[1];

        List<DashboardClassRowDto> scheduleRows = gymClassRepository
            .findAll(
                DashboardGymClassSpecifications.buildClassScheduleFilter(clean(search), activityId, teacherId),
                Sort.by(Sort.Direction.DESC, "allDay")
                    .and(Sort.by(Sort.Direction.ASC, "startTime"))
                    .and(Sort.by(Sort.Direction.ASC, "className"))
            )
            .stream()
            .flatMap(item -> buildScheduleRows(item, effectiveFrom, effectiveTo).stream())
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

        for (GymClassEntity item : gymClassRepository.findAll()) {
            int occurrences = countOccurrences(item, range[0], range[1]);
            if (occurrences <= 0) {
                continue;
            }

            Long activityId = item.getActivity() == null ? null : item.getActivity().getId();
            String activityName = item.getActivity() == null ? "Sin actividad" : item.getActivity().getName();
            if (safeSearch != null && !activityName.toLowerCase().contains(safeSearch.toLowerCase())) {
                continue;
            }

            String key = (activityId == null ? "null" : activityId.toString()) + "|" + activityName;
            ActivityRankingAccumulator accumulator = aggregations.computeIfAbsent(
                key,
                ignored -> new ActivityRankingAccumulator(activityId, activityName)
            );
            accumulator.add(item, occurrences);
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

    private List<DashboardClassRowDto> buildScheduleRows(GymClassEntity item, LocalDate fromDate, LocalDate toDate) {
        List<DashboardClassRowDto> rows = new ArrayList<>();
        Set<WeekDay> weekDays = resolveWeekDays(item);
        boolean allDay = Boolean.TRUE.equals(item.getAllDay());
        LocalTime safeStartTime = null;
        LocalTime safeEndTime = null;
        if (!allDay) {
            safeStartTime = item.getStartTime() == null ? LocalTime.NOON : item.getStartTime();
            safeEndTime = item.getEndTime() == null ? safeStartTime.plusHours(1) : item.getEndTime();
        }
        double occupancy = item.getMaxCapacity() == null || item.getMaxCapacity() <= 0
            ? 0d
            : (item.getEnrolled() * 100.0) / item.getMaxCapacity();

        LocalDate cursor = fromDate;
        while (!cursor.isAfter(toDate)) {
            WeekDay day = toWeekDay(cursor.getDayOfWeek().getValue());
            if (weekDays.contains(day)) {
                rows.add(DashboardClassRowDto.builder()
                    .classId(item.getId())
                    .className(item.getClassName())
                    .activityName(item.getActivity() == null ? "Sin actividad" : item.getActivity().getName())
                    .teacherName(item.getTeacher() == null ? "Sin profesor" : item.getTeacher().getFirstName() + " " + item.getTeacher().getLastName())
                    .date(cursor)
                    .weekDay(day.getValue())
                    .allDay(allDay)
                    .startTime(safeStartTime)
                    .endTime(safeEndTime)
                    .enrolled(item.getEnrolled())
                    .maxCapacity(item.getMaxCapacity())
                    .occupancyPercent(occupancy)
                    .build());
            }
            cursor = cursor.plusDays(1);
        }

        return rows;
    }

    private Set<WeekDay> resolveWeekDays(GymClassEntity item) {
        if (item.getWeekDays() != null && !item.getWeekDays().isEmpty()) {
            return item.getWeekDays();
        }
        LinkedHashSet<WeekDay> fallback = new LinkedHashSet<>();
        if (item.getDate() != null) {
            fallback.add(toWeekDay(item.getDate().getDayOfWeek().getValue()));
        }
        return fallback;
    }

    private int countOccurrences(GymClassEntity item, LocalDate fromDate, LocalDate toDate) {
        Set<WeekDay> days = resolveWeekDays(item);
        if (days.isEmpty()) {
            return 0;
        }
        int total = 0;
        LocalDate cursor = fromDate;
        while (!cursor.isAfter(toDate)) {
            WeekDay day = toWeekDay(cursor.getDayOfWeek().getValue());
            if (days.contains(day)) {
                total++;
            }
            cursor = cursor.plusDays(1);
        }
        return total;
    }

    private WeekDay toWeekDay(int dayOfWeekValue) {
        return switch (dayOfWeekValue) {
            case 1 -> WeekDay.LUNES;
            case 2 -> WeekDay.MARTES;
            case 3 -> WeekDay.MIERCOLES;
            case 4 -> WeekDay.JUEVES;
            case 5 -> WeekDay.VIERNES;
            case 6 -> WeekDay.SABADO;
            default -> WeekDay.DOMINGO;
        };
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

        private void add(GymClassEntity item, int occurrences) {
            classesCount += occurrences;
            totalEnrolled += (long) item.getEnrolled() * occurrences;
            if (item.getMaxCapacity() != null && item.getMaxCapacity() > 0) {
                double occupancy = (item.getEnrolled() * 100.0) / item.getMaxCapacity();
                occupancySum += occupancy * occurrences;
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
