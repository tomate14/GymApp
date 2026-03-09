package com.fitexa.repository;

import com.fitexa.entities.GymClassSubscriptionEntity;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GymClassSubscriptionRepository extends JpaRepository<GymClassSubscriptionEntity, Long> {
    long countByGymClassId(Long gymClassId);
    boolean existsByGymClassIdAndUserId(Long gymClassId, Long userId);
    List<GymClassSubscriptionEntity> findByUserId(Long userId);

    @Query("""
        select concat(t.firstName, ' ', t.lastName) as teacherName,
               count(distinct s.user.id) as studentsCount
        from GymClassSubscriptionEntity s
        join s.gymClass gc
        join gc.teacher t
        where s.user.userType = com.fitexa.entities.UserType.ALUMNO
          and (:fromDate is null or gc.date >= :fromDate)
          and (:toDate is null or gc.date <= :toDate)
        group by t.id, t.firstName, t.lastName
        order by count(distinct s.user.id) desc
        """)
    List<DashboardStudentsByTeacherProjection> countStudentsByTeacherInRange(
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );
}
