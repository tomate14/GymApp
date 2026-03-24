package com.fitexa.services.impl.dashboard;

import com.fitexa.entities.GymClassEntity;
import com.fitexa.entities.WeekDay;
import com.fitexa.repository.DashboardClassSubscriptionCountProjection;
import com.fitexa.repository.GymClassRepository;
import com.fitexa.repository.GymClassSubscriptionRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardClassMetricsService {

    private final GymClassRepository gymClassRepository;
    private final GymClassSubscriptionRepository gymClassSubscriptionRepository;

    @Transactional(readOnly = true)
    public List<DashboardClassOccurrenceMetric> listOccurrences(LocalDate fromDate, LocalDate toDate) {
        return buildOccurrences(gymClassRepository.findAll(), fromDate, toDate);
    }

    @Transactional(readOnly = true)
    public List<DashboardClassOccurrenceMetric> listOccurrences(
        LocalDate fromDate,
        LocalDate toDate,
        Specification<GymClassEntity> specification
    ) {
        List<GymClassEntity> classes = specification == null
            ? gymClassRepository.findAll()
            : gymClassRepository.findAll(specification);
        return buildOccurrences(classes, fromDate, toDate);
    }

    private List<DashboardClassOccurrenceMetric> buildOccurrences(
        List<GymClassEntity> classes,
        LocalDate fromDate,
        LocalDate toDate
    ) {
        if (classes.isEmpty() || fromDate == null || toDate == null || fromDate.isAfter(toDate)) {
            return List.of();
        }

        Map<String, Integer> subscriptionsByClassAndDate = loadSubscriptions(classes, fromDate, toDate);
        List<DashboardClassOccurrenceMetric> occurrences = new ArrayList<>();

        for (GymClassEntity gymClass : classes) {
            occurrences.addAll(buildOccurrencesForClass(gymClass, fromDate, toDate, subscriptionsByClassAndDate));
        }

        occurrences.sort(
            Comparator.comparing(DashboardClassOccurrenceMetric::getDate)
                .thenComparing(item -> item.isAllDay() ? 0 : 1)
                .thenComparing(DashboardClassOccurrenceMetric::getStartTime, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(item -> item.getGymClass().getClassName(), String.CASE_INSENSITIVE_ORDER)
        );
        return occurrences;
    }

    private Map<String, Integer> loadSubscriptions(List<GymClassEntity> classes, LocalDate fromDate, LocalDate toDate) {
        List<Long> classIds = classes.stream()
            .map(GymClassEntity::getId)
            .filter(java.util.Objects::nonNull)
            .toList();

        if (classIds.isEmpty()) {
            return Map.of();
        }

        Map<String, Integer> counts = new HashMap<>();
        for (DashboardClassSubscriptionCountProjection item :
            gymClassSubscriptionRepository.countSubscriptionsByGymClassIdsAndTargetDateBetween(classIds, fromDate, toDate)) {
            counts.put(key(item.getGymClassId(), item.getTargetDate()), item.getSubscribedCount().intValue());
        }
        return counts;
    }

    private List<DashboardClassOccurrenceMetric> buildOccurrencesForClass(
        GymClassEntity gymClass,
        LocalDate fromDate,
        LocalDate toDate,
        Map<String, Integer> subscriptionsByClassAndDate
    ) {
        List<DashboardClassOccurrenceMetric> occurrences = new ArrayList<>();
        LocalDate activeFrom = gymClass.getDate() == null || gymClass.getDate().isBefore(fromDate) ? fromDate : gymClass.getDate();
        if (activeFrom.isAfter(toDate)) {
            return occurrences;
        }

        Set<WeekDay> weekDays = resolveWeekDays(gymClass);
        if (weekDays.isEmpty()) {
            if (gymClass.getDate() != null && !gymClass.getDate().isBefore(fromDate) && !gymClass.getDate().isAfter(toDate)) {
                occurrences.add(toOccurrence(gymClass, gymClass.getDate(), subscriptionsByClassAndDate));
            }
            return occurrences;
        }

        LocalDate cursor = activeFrom;
        while (!cursor.isAfter(toDate)) {
            if (weekDays.contains(toWeekDay(cursor))) {
                occurrences.add(toOccurrence(gymClass, cursor, subscriptionsByClassAndDate));
            }
            cursor = cursor.plusDays(1);
        }

        return occurrences;
    }

    private DashboardClassOccurrenceMetric toOccurrence(
        GymClassEntity gymClass,
        LocalDate date,
        Map<String, Integer> subscriptionsByClassAndDate
    ) {
        int enrolled = subscriptionsByClassAndDate.getOrDefault(key(gymClass.getId(), date), 0);
        Integer maxCapacity = gymClass.getMaxCapacity();
        double occupancyPercent = maxCapacity == null || maxCapacity <= 0
            ? 0d
            : (enrolled * 100.0) / maxCapacity;
        boolean allDay = Boolean.TRUE.equals(gymClass.getAllDay());
        LocalTime safeStartTime = allDay ? null : (gymClass.getStartTime() == null ? LocalTime.NOON : gymClass.getStartTime());
        LocalTime safeEndTime = allDay ? null : (gymClass.getEndTime() == null ? safeStartTime.plusHours(1) : gymClass.getEndTime());

        return DashboardClassOccurrenceMetric.builder()
            .gymClass(gymClass)
            .date(date)
            .activityId(gymClass.getActivity() == null ? null : gymClass.getActivity().getId())
            .activityName(gymClass.getActivity() == null ? "Sin actividad" : gymClass.getActivity().getName())
            .teacherId(gymClass.getTeacher() == null ? null : gymClass.getTeacher().getId())
            .teacherName(gymClass.getTeacher() == null ? "Sin profesor" : gymClass.getTeacher().getFirstName() + " " + gymClass.getTeacher().getLastName())
            .allDay(allDay)
            .startTime(safeStartTime)
            .endTime(safeEndTime)
            .enrolled(enrolled)
            .maxCapacity(maxCapacity)
            .occupancyPercent(occupancyPercent)
            .build();
    }

    private Set<WeekDay> resolveWeekDays(GymClassEntity gymClass) {
        if (gymClass.getWeekDays() != null && !gymClass.getWeekDays().isEmpty()) {
            return gymClass.getWeekDays();
        }
        LinkedHashSet<WeekDay> fallback = new LinkedHashSet<>();
        if (gymClass.getDate() != null) {
            fallback.add(toWeekDay(gymClass.getDate()));
        }
        return fallback;
    }

    private WeekDay toWeekDay(LocalDate date) {
        return switch (date.getDayOfWeek().getValue()) {
            case 1 -> WeekDay.LUNES;
            case 2 -> WeekDay.MARTES;
            case 3 -> WeekDay.MIERCOLES;
            case 4 -> WeekDay.JUEVES;
            case 5 -> WeekDay.VIERNES;
            case 6 -> WeekDay.SABADO;
            default -> WeekDay.DOMINGO;
        };
    }

    private String key(Long gymClassId, LocalDate targetDate) {
        return gymClassId + "|" + targetDate;
    }
}
