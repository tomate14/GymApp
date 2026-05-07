package com.fitexa.utils;

import com.fitexa.entities.PaymentStatus;
import com.fitexa.entities.UserEntity;
import com.fitexa.entities.UserType;
import java.time.LocalDate;
import org.springframework.data.jpa.domain.Specification;

public final class DashboardUserSpecifications {
    private DashboardUserSpecifications() {}

    public static Specification<UserEntity> buildRenewalsFilter(
        Long clientId,
        String search,
        Long planId,
        PaymentStatus paymentStatus,
        LocalDate renewalFrom,
        LocalDate renewalTo
    ) {
        return Specification.where(byClient(clientId))
            .and(studentsOnly())
            .and(bySearch(search))
            .and(byPlan(planId))
            .and(byPaymentStatus(paymentStatus))
            .and(byRenewalFrom(renewalFrom))
            .and(byRenewalTo(renewalTo));
    }

    private static Specification<UserEntity> byClient(Long clientId) {
        return (root, query, cb) -> cb.equal(root.get("client").get("id"), clientId);
    }

    private static Specification<UserEntity> studentsOnly() {
        return (root, query, cb) -> cb.equal(root.get("userType"), UserType.ALUMNO);
    }

    private static Specification<UserEntity> bySearch(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        String like = "%" + search.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
            cb.like(cb.lower(root.get("firstName")), like),
            cb.like(cb.lower(root.get("lastName")), like),
            cb.like(cb.lower(cb.concat(cb.concat(root.get("firstName"), " "), root.get("lastName"))), like),
            cb.like(root.get("dni"), "%" + search.trim() + "%")
        );
    }

    private static Specification<UserEntity> byPlan(Long planId) {
        if (planId == null || planId <= 0) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("plan").get("id"), planId);
    }

    private static Specification<UserEntity> byPaymentStatus(PaymentStatus paymentStatus) {
        if (paymentStatus == null) {
            return null;
        }
        LocalDate today = LocalDate.now();
        if (paymentStatus == PaymentStatus.AL_DIA) {
            return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("renewalDate"), today);
        }
        return (root, query, cb) -> cb.lessThan(root.get("renewalDate"), today);
    }

    private static Specification<UserEntity> byRenewalFrom(LocalDate renewalFrom) {
        if (renewalFrom == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("renewalDate"), renewalFrom);
    }

    private static Specification<UserEntity> byRenewalTo(LocalDate renewalTo) {
        if (renewalTo == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("renewalDate"), renewalTo);
    }
}
