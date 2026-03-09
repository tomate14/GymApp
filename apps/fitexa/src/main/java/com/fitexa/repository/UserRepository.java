package com.fitexa.repository;

import com.fitexa.entities.PaymentStatus;
import com.fitexa.entities.UserEntity;
import com.fitexa.entities.UserType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<UserEntity, Long>, JpaSpecificationExecutor<UserEntity> {

    Optional<UserEntity> findByDni(String dni);
    Optional<UserEntity> findByEmailIgnoreCase(String email);

    long countByUserType(UserType userType);

    long countByUserTypeAndRegistrationDateBetween(UserType userType, LocalDate fromDate, LocalDate toDate);

    long countByUserTypeAndPaymentStatus(UserType userType, PaymentStatus paymentStatus);

    long countByUserTypeAndPaymentStatusAndRegistrationDateBetween(
        UserType userType,
        PaymentStatus paymentStatus,
        LocalDate fromDate,
        LocalDate toDate
    );

    long countByUserTypeAndRenewalDateBetween(UserType userType, LocalDate fromDate, LocalDate toDate);

    @Query("""
        select count(u.id)
        from UserEntity u
        where u.userType = :userType
          and u.renewalDate >= :fromDate
          and u.renewalDate <= :toDate
          and u.renewalDate < :referenceDate
        """)
    long countByUserTypeAndExpiredRenewalDateInRange(
        @Param("userType") UserType userType,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate,
        @Param("referenceDate") LocalDate referenceDate
    );

    long countByUserTypeAndPlanIsNull(UserType userType);

    long countByUserTypeAndPlanIsNullAndRegistrationDateBetween(UserType userType, LocalDate fromDate, LocalDate toDate);

    @Query("""
        select coalesce(sum(p.price), 0)
        from UserEntity u
        join u.plan p
        where u.userType = :userType
        """)
    BigDecimal sumEstimatedRevenueByUserType(@Param("userType") UserType userType);

    @Query("""
        select coalesce(sum(p.price), 0)
        from UserEntity u
        join u.plan p
        where u.userType = :userType
          and u.registrationDate >= :fromDate
          and u.registrationDate <= :toDate
        """)
    BigDecimal sumEstimatedRevenueByUserTypeAndRegistrationDateBetween(
        @Param("userType") UserType userType,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );

    @Query("""
        select p.id, coalesce(p.name, 'Sin plan'), count(u.id)
        from UserEntity u
        left join u.plan p
        where u.userType = :userType
        group by p.id, p.name
        order by count(u.id) desc
        """)
    List<Object[]> countUsersByPlan(@Param("userType") UserType userType);

    @Query("""
        select p.id, coalesce(p.name, 'Sin plan'), count(u.id)
        from UserEntity u
        left join u.plan p
        where u.userType = :userType
          and u.registrationDate >= :fromDate
          and u.registrationDate <= :toDate
        group by p.id, p.name
        order by count(u.id) desc
        """)
    List<Object[]> countUsersByPlanInRegistrationRange(
        @Param("userType") UserType userType,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );

    @Query("""
        select u.paymentStatus, count(u.id)
        from UserEntity u
        where u.userType = :userType
        group by u.paymentStatus
        """)
    List<Object[]> countUsersByPaymentStatus(@Param("userType") UserType userType);

    @Query("""
        select u.paymentStatus, count(u.id)
        from UserEntity u
        where u.userType = :userType
          and u.registrationDate >= :fromDate
          and u.registrationDate <= :toDate
        group by u.paymentStatus
        """)
    List<Object[]> countUsersByPaymentStatusInRegistrationRange(
        @Param("userType") UserType userType,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );

    @Query(
        value = """
            select date_format(u.registration_date, '%Y-%m') as period, count(u.id) as total
            from users u
            where u.user_type = 'ALUMNO'
              and u.registration_date >= :fromDate
              and u.registration_date <= :toDate
            group by date_format(u.registration_date, '%Y-%m')
            order by period asc
            """,
        nativeQuery = true
    )
    List<Object[]> countStudentRegistrationsByMonth(
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );

    @Query(
        value = """
            select cast(year(u.registration_date) as char) as period, count(u.id) as total
            from users u
            where u.user_type = 'ALUMNO'
              and u.registration_date >= :fromDate
              and u.registration_date <= :toDate
            group by year(u.registration_date)
            order by period asc
            """,
        nativeQuery = true
    )
    List<Object[]> countStudentRegistrationsByYear(
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );

    @Query("""
        select u
        from UserEntity u
        where (:userType is null or u.userType = :userType)
          and (:name is null or lower(concat(u.firstName, ' ', u.lastName)) like lower(concat('%', :name, '%')))
          and (:dni is null or u.dni like concat('%', :dni, '%'))
          and (:paymentStatus is null or u.paymentStatus = :paymentStatus)
          and (:registrationFrom is null or u.registrationDate >= :registrationFrom)
          and (:registrationTo is null or u.registrationDate <= :registrationTo)
          and (:renewalFrom is null or u.renewalDate >= :renewalFrom)
          and (:renewalTo is null or u.renewalDate <= :renewalTo)
        """)
    Page<UserEntity> findForTable(
        @Param("userType") UserType userType,
        @Param("name") String name,
        @Param("dni") String dni,
        @Param("paymentStatus") PaymentStatus paymentStatus,
        @Param("registrationFrom") LocalDate registrationFrom,
        @Param("registrationTo") LocalDate registrationTo,
        @Param("renewalFrom") LocalDate renewalFrom,
        @Param("renewalTo") LocalDate renewalTo,
        Pageable pageable
    );
}
