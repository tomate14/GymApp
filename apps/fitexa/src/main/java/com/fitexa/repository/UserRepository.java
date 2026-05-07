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

    Optional<UserEntity> findByDniAndClientId(String dni, Long clientId);
    Optional<UserEntity> findByEmailIgnoreCase(String email);
    Optional<UserEntity> findByEmailIgnoreCaseAndClientId(String email, Long clientId);
    Optional<UserEntity> findByIdAndClientId(Long id, Long clientId);
    boolean existsByIdAndClientId(Long id, Long clientId);
    long countByRoleIdAndClientId(Long roleId, Long clientId);

    long countByUserTypeAndClientId(UserType userType, Long clientId);

    long countByUserTypeAndClientIdAndRegistrationDateBetween(UserType userType, Long clientId, LocalDate fromDate, LocalDate toDate);

    long countByUserTypeAndClientIdAndRenewalDateBetween(UserType userType, Long clientId, LocalDate fromDate, LocalDate toDate);

    @Query("""
        select count(u.id)
        from UserEntity u
        where u.userType = :userType
          and u.client.id = :clientId
          and u.renewalDate >= :fromDate
          and u.renewalDate <= :toDate
          and u.renewalDate < :referenceDate
        """)
    long countByUserTypeAndExpiredRenewalDateInRange(
        @Param("userType") UserType userType,
        @Param("clientId") Long clientId,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate,
        @Param("referenceDate") LocalDate referenceDate
    );

    long countByUserTypeAndClientIdAndPlanIsNull(UserType userType, Long clientId);

    long countByUserTypeAndClientIdAndPlanIsNullAndRegistrationDateBetween(UserType userType, Long clientId, LocalDate fromDate, LocalDate toDate);

    @Query("""
        select coalesce(sum(p.price), 0)
        from UserEntity u
        join u.plan p
        where u.userType = :userType
          and u.client.id = :clientId
        """)
    BigDecimal sumEstimatedRevenueByUserType(@Param("userType") UserType userType, @Param("clientId") Long clientId);

    @Query("""
        select coalesce(sum(p.price), 0)
        from UserEntity u
        join u.plan p
        where u.userType = :userType
          and u.client.id = :clientId
          and u.registrationDate >= :fromDate
          and u.registrationDate <= :toDate
        """)
    BigDecimal sumEstimatedRevenueByUserTypeAndRegistrationDateBetween(
        @Param("userType") UserType userType,
        @Param("clientId") Long clientId,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );

    @Query("""
        select p.id, coalesce(p.name, 'Sin plan'), count(u.id)
        from UserEntity u
        left join u.plan p
        where u.userType = :userType
          and u.client.id = :clientId
        group by p.id, p.name
        order by count(u.id) desc
        """)
    List<Object[]> countUsersByPlan(@Param("userType") UserType userType, @Param("clientId") Long clientId);

    @Query("""
        select p.id, coalesce(p.name, 'Sin plan'), count(u.id)
        from UserEntity u
        left join u.plan p
        where u.userType = :userType
          and u.client.id = :clientId
          and u.registrationDate >= :fromDate
          and u.registrationDate <= :toDate
        group by p.id, p.name
        order by count(u.id) desc
        """)
    List<Object[]> countUsersByPlanInRegistrationRange(
        @Param("userType") UserType userType,
        @Param("clientId") Long clientId,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );

    @Query("""
        select p.id, p.name, count(u.id)
        from UserEntity u
        join u.plan p
        where u.userType = :userType
          and u.client.id = :clientId
          and u.renewalDate >= :referenceDate
          and u.registrationDate <= :referenceDate
        group by p.id, p.name
        order by count(u.id) desc
        """)
    List<Object[]> countActiveUsersByPlan(
        @Param("userType") UserType userType,
        @Param("clientId") Long clientId,
        @Param("referenceDate") LocalDate referenceDate
    );

    @Query("""
        select case when u.renewalDate >= :referenceDate then 'AL_DIA' else 'ADEUDA' end, count(u.id)
        from UserEntity u
        where u.userType = :userType
          and u.client.id = :clientId
        group by case when u.renewalDate >= :referenceDate then 'AL_DIA' else 'ADEUDA' end
        """)
    List<Object[]> countUsersByPaymentStatus(
        @Param("userType") UserType userType,
        @Param("clientId") Long clientId,
        @Param("referenceDate") LocalDate referenceDate
    );

    @Query("""
        select case when u.renewalDate >= :referenceDate then 'AL_DIA' else 'ADEUDA' end, count(u.id)
        from UserEntity u
        where u.userType = :userType
          and u.client.id = :clientId
          and u.registrationDate >= :fromDate
          and u.registrationDate <= :toDate
        group by case when u.renewalDate >= :referenceDate then 'AL_DIA' else 'ADEUDA' end
        """)
    List<Object[]> countUsersByPaymentStatusInRegistrationRange(
        @Param("userType") UserType userType,
        @Param("clientId") Long clientId,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate,
        @Param("referenceDate") LocalDate referenceDate
    );

    @Query("""
        select case when u.renewalDate >= :referenceDate then 'AL_DIA' else 'ADEUDA' end, count(u.id)
        from UserEntity u
        where u.userType = :userType
          and u.client.id = :clientId
          and u.registrationDate <= :referenceDate
        group by case when u.renewalDate >= :referenceDate then 'AL_DIA' else 'ADEUDA' end
        """)
    List<Object[]> countUsersByPaymentStatusUpToDate(
        @Param("userType") UserType userType,
        @Param("clientId") Long clientId,
        @Param("referenceDate") LocalDate referenceDate
    );

    @Query(
        value = """
            select date_format(u.registration_date, '%Y-%m') as period, count(u.id) as total
            from users u
            where u.user_type = 'ALUMNO'
              and u.client_id = :clientId
              and u.registration_date >= :fromDate
              and u.registration_date <= :toDate
            group by date_format(u.registration_date, '%Y-%m')
            order by period asc
            """,
        nativeQuery = true
    )
    List<Object[]> countStudentRegistrationsByMonth(
        @Param("clientId") Long clientId,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );

    @Query(
        value = """
            select cast(year(u.registration_date) as char) as period, count(u.id) as total
            from users u
            where u.user_type = 'ALUMNO'
              and u.client_id = :clientId
              and u.registration_date >= :fromDate
              and u.registration_date <= :toDate
            group by cast(year(u.registration_date) as char)
            order by cast(year(u.registration_date) as char) asc
            """,
        nativeQuery = true
    )
    List<Object[]> countStudentRegistrationsByYear(
        @Param("clientId") Long clientId,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );

    @Query("""
        select u
        from UserEntity u
        where u.client.id = :clientId
          and (:userType is null or u.userType = :userType)
          and (:roleId is null or (u.role is not null and u.role.id = :roleId))
          and (
            :name is null
            or lower(concat(u.firstName, ' ', u.lastName)) like lower(concat('%', :name, '%'))
            or lower(u.email) like lower(concat('%', :name, '%'))
          )
          and (:dni is null or u.dni like concat('%', :dni, '%'))
          and (
            :filterPaymentStatus = false
            or (:paymentStatusAlDia = true and u.renewalDate >= :referenceDate)
            or (:paymentStatusAlDia = false and u.renewalDate < :referenceDate)
          )
          and (:registrationFrom is null or u.registrationDate >= :registrationFrom)
          and (:registrationTo is null or u.registrationDate <= :registrationTo)
          and (:renewalFrom is null or u.renewalDate >= :renewalFrom)
          and (:renewalTo is null or u.renewalDate <= :renewalTo)
        """)
    Page<UserEntity> findForTable(
        @Param("clientId") Long clientId,
        @Param("userType") UserType userType,
        @Param("roleId") Long roleId,
        @Param("name") String name,
        @Param("dni") String dni,
        @Param("filterPaymentStatus") boolean filterPaymentStatus,
        @Param("paymentStatusAlDia") boolean paymentStatusAlDia,
        @Param("referenceDate") LocalDate referenceDate,
        @Param("registrationFrom") LocalDate registrationFrom,
        @Param("registrationTo") LocalDate registrationTo,
        @Param("renewalFrom") LocalDate renewalFrom,
        @Param("renewalTo") LocalDate renewalTo,
        Pageable pageable
    );

    List<UserEntity> findAllByClientId(Long clientId);
    List<UserEntity> findAllByClientIdAndUserType(Long clientId, UserType userType);
}
