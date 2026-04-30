package com.fitexa.repository;

import com.fitexa.entities.GymClassSubscriptionEntity;
import java.util.Collection;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GymClassSubscriptionRepository extends JpaRepository<GymClassSubscriptionEntity, Long> {
    long countByGymClassIdAndTargetDate(Long gymClassId, LocalDate targetDate);
    boolean existsByGymClassIdAndUserIdAndTargetDate(Long gymClassId, Long userId, LocalDate targetDate);
    List<GymClassSubscriptionEntity> findByUserIdAndTargetDate(Long userId, LocalDate targetDate);
    List<GymClassSubscriptionEntity> findByUserIdAndTargetDateBetween(Long userId, LocalDate fromDate, LocalDate toDate);
    List<GymClassSubscriptionEntity> findByGymClassIdAndTargetDate(Long gymClassId, LocalDate targetDate);

    @Query("""
        select s
        from GymClassSubscriptionEntity s
        join fetch s.user u
        where s.gymClass.id = :gymClassId
          and s.targetDate = :targetDate
        order by u.lastName asc, u.firstName asc
        """)
    List<GymClassSubscriptionEntity> findDetailedByGymClassIdAndTargetDate(
        @Param("gymClassId") Long gymClassId,
        @Param("targetDate") LocalDate targetDate
    );

    @Query("""
        select s.gymClass.id as gymClassId,
               s.targetDate as targetDate,
               count(s.id) as attendanceCount
        from GymClassSubscriptionEntity s
        where s.gymClass.id in :gymClassIds
          and s.targetDate >= :fromDate
          and s.targetDate <= :toDate
          and s.attendancePresent = true
        group by s.gymClass.id, s.targetDate
        """)
    List<DashboardClassAttendanceCountProjection> countAttendanceByGymClassIdsAndTargetDateBetween(
        @Param("gymClassIds") Collection<Long> gymClassIds,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );

    @Query("""
        select s.gymClass.id as gymClassId,
               s.targetDate as targetDate,
               count(s.id) as subscribedCount
        from GymClassSubscriptionEntity s
        where s.gymClass.id in :gymClassIds
          and s.targetDate >= :fromDate
          and s.targetDate <= :toDate
        group by s.gymClass.id, s.targetDate
        """)
    List<DashboardClassSubscriptionCountProjection> countSubscriptionsByGymClassIdsAndTargetDateBetween(
        @Param("gymClassIds") Collection<Long> gymClassIds,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );

    @Query("""
        select concat(t.firstName, ' ', t.lastName) as teacherName,
               count(distinct s.user.id) as studentsCount
        from GymClassSubscriptionEntity s
        join s.gymClass gc
        join gc.teacher t
        where s.user.userType = com.fitexa.entities.UserType.ALUMNO
          and (:fromDate is null or s.targetDate >= :fromDate)
          and (:toDate is null or s.targetDate <= :toDate)
        group by t.id, t.firstName, t.lastName
        order by count(distinct s.user.id) desc
        """)
    List<DashboardStudentsByTeacherProjection> countStudentsByTeacherInRange(
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );

    @Query("""
        select concat(t.firstName, ' ', t.lastName) as teacherName,
               count(distinct s.user.id) as studentsCount
        from GymClassSubscriptionEntity s
        join s.gymClass gc
        join gc.teacher t
        join s.user u
        where u.userType = com.fitexa.entities.UserType.ALUMNO
          and u.renewalDate >= :referenceDate
          and u.registrationDate <= :referenceDate
          and s.targetDate <= :referenceDate
        group by t.id, t.firstName, t.lastName
        order by count(distinct s.user.id) desc
        """)
    List<DashboardStudentsByTeacherProjection> countActiveStudentsByTeacher(
        @Param("referenceDate") LocalDate referenceDate
    );
}
