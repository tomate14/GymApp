package com.fitexa.utils;

import com.fitexa.entities.GymClassEntity;
import java.time.LocalDate;
import org.springframework.data.jpa.domain.Specification;

public final class DashboardGymClassSpecifications {
    private DashboardGymClassSpecifications() {}

    public static Specification<GymClassEntity> buildClassScheduleFilter(
        String search,
        Long activityId,
        Long teacherId,
        LocalDate dateFrom,
        LocalDate dateTo
    ) {
        return Specification.where(bySearch(search))
            .and(byActivity(activityId))
            .and(byTeacher(teacherId))
            .and(byDateFrom(dateFrom))
            .and(byDateTo(dateTo));
    }

    private static Specification<GymClassEntity> bySearch(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        String like = "%" + search.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("className")), like);
    }

    private static Specification<GymClassEntity> byActivity(Long activityId) {
        if (activityId == null || activityId <= 0) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("activity").get("id"), activityId);
    }

    private static Specification<GymClassEntity> byTeacher(Long teacherId) {
        if (teacherId == null || teacherId <= 0) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("teacher").get("id"), teacherId);
    }

    private static Specification<GymClassEntity> byDateFrom(LocalDate dateFrom) {
        if (dateFrom == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("date"), dateFrom);
    }

    private static Specification<GymClassEntity> byDateTo(LocalDate dateTo) {
        if (dateTo == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("date"), dateTo);
    }
}
