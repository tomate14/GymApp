package com.fitexa.repository;

import com.fitexa.entities.UserPaymentMovementEntity;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserPaymentMovementRepository extends JpaRepository<UserPaymentMovementEntity, Long> {
    List<UserPaymentMovementEntity> findByUserIdOrderByPaidAtDesc(Long userId);
    Optional<UserPaymentMovementEntity> findByIdAndUserId(Long id, Long userId);

    @Query("""
        select count(m.id)
        from UserPaymentMovementEntity m
        where m.detail = :detail
          and m.paidAt >= :fromDateTime
          and m.paidAt <= :toDateTime
        """)
    long countByDetailAndPaidAtBetween(
        @Param("detail") String detail,
        @Param("fromDateTime") LocalDateTime fromDateTime,
        @Param("toDateTime") LocalDateTime toDateTime
    );

    @Query("""
        select count(distinct m.user.id)
        from UserPaymentMovementEntity m
        where m.detail = :detail
          and (
            (:paymentStatusAlDia = true and m.user.renewalDate >= :referenceDate)
            or (:paymentStatusAlDia = false and m.user.renewalDate < :referenceDate)
          )
          and m.paidAt >= :fromDateTime
          and m.paidAt <= :toDateTime
        """)
    long countDistinctUsersByDetailAndPaymentStatusAndPaidAtBetween(
        @Param("detail") String detail,
        @Param("paymentStatusAlDia") boolean paymentStatusAlDia,
        @Param("referenceDate") LocalDate referenceDate,
        @Param("fromDateTime") LocalDateTime fromDateTime,
        @Param("toDateTime") LocalDateTime toDateTime
    );

    @Query("""
        select coalesce(sum(m.amount), 0)
        from UserPaymentMovementEntity m
        where m.detail = :detail
          and m.paidAt >= :fromDateTime
          and m.paidAt <= :toDateTime
        """)
    BigDecimal sumAmountByDetailAndPaidAtBetween(
        @Param("detail") String detail,
        @Param("fromDateTime") LocalDateTime fromDateTime,
        @Param("toDateTime") LocalDateTime toDateTime
    );
}
