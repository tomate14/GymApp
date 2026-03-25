package com.fitexa.repository;

import com.fitexa.entities.UserPaymentMovementEntity;
import com.fitexa.entities.PaymentStatus;
import java.math.BigDecimal;
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
          and m.user.paymentStatus = :paymentStatus
          and m.paidAt >= :fromDateTime
          and m.paidAt <= :toDateTime
        """)
    long countDistinctUsersByDetailAndPaymentStatusAndPaidAtBetween(
        @Param("detail") String detail,
        @Param("paymentStatus") PaymentStatus paymentStatus,
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
