package com.fitexa.repository;

import com.fitexa.entities.RoutineAssignmentEntity;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoutineAssignmentRepository extends JpaRepository<RoutineAssignmentEntity, Long> {

    List<RoutineAssignmentEntity> findByUserIdOrderByStartDateDescIdDesc(Long userId);

    @Query("""
        select ra
        from RoutineAssignmentEntity ra
        where ra.user.id = :userId
          and (:excludeId is null or ra.id <> :excludeId)
          and ra.startDate <= :endDate
          and ra.endDate >= :startDate
        """)
    List<RoutineAssignmentEntity> findOverlappingAssignments(
        @Param("userId") Long userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("excludeId") Long excludeId
    );

    @Query("""
        select count(ra.id)
        from RoutineAssignmentEntity ra
        where ra.startDate <= :referenceDate
          and ra.endDate >= :referenceDate
        """)
    long countCurrentAssignments(@Param("referenceDate") LocalDate referenceDate);

    @Query("""
        select count(ra.id)
        from RoutineAssignmentEntity ra
        where ra.startDate <= :toDate
          and ra.endDate >= :fromDate
        """)
    long countAssignmentsOverlappingRange(
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );

    @Query("""
        select count(ra.id)
        from RoutineAssignmentEntity ra
        where ra.endDate < :referenceDate
        """)
    long countExpiredAssignments(@Param("referenceDate") LocalDate referenceDate);

    @Query("""
        select count(ra.id)
        from RoutineAssignmentEntity ra
        where ra.endDate >= :fromDate
          and ra.endDate <= :toDate
        """)
    long countAssignmentsEndedInRange(
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );

    @Query(
        value = """
            select u.id as userId,
                   u.first_name as firstName,
                   u.last_name as lastName,
                   u.dni as dni,
                   p.name as planName,
                   p.max_days_per_week as maxDaysPerWeek,
                   count(distinct rad.day) as assignedDays
            from users u
            join plans p on p.id = u.plan_id
            join routine_assignments ra on ra.user_id = u.id
                and ra.start_date <= :referenceDate
                and ra.end_date >= :referenceDate
            join routine_assignment_days rad on rad.routine_assignment_id = ra.id
            where u.user_type = 'ALUMNO'
              and (
                :search is null
                or lower(concat(u.first_name, ' ', u.last_name)) like lower(concat('%', :search, '%'))
                or u.dni like concat('%', :search, '%')
              )
            group by u.id, u.first_name, u.last_name, u.dni, p.name, p.max_days_per_week
            having count(distinct rad.day) > p.max_days_per_week
            order by count(distinct rad.day) desc, u.last_name asc, u.first_name asc
            """,
        countQuery = """
            select count(*) from (
                select u.id
                from users u
                join plans p on p.id = u.plan_id
                join routine_assignments ra on ra.user_id = u.id
                    and ra.start_date <= :referenceDate
                    and ra.end_date >= :referenceDate
                join routine_assignment_days rad on rad.routine_assignment_id = ra.id
                where u.user_type = 'ALUMNO'
                  and (
                    :search is null
                    or lower(concat(u.first_name, ' ', u.last_name)) like lower(concat('%', :search, '%'))
                    or u.dni like concat('%', :search, '%')
                  )
                group by u.id, p.max_days_per_week
                having count(distinct rad.day) > p.max_days_per_week
            ) overloads
            """,
        nativeQuery = true
    )
    Page<DashboardPlanOverloadProjection> findPlanOverloads(
        @Param("referenceDate") LocalDate referenceDate,
        @Param("search") String search,
        Pageable pageable
    );

    @Query(
        value = """
            select u.id as userId,
                   u.first_name as firstName,
                   u.last_name as lastName,
                   u.dni as dni,
                   p.name as planName,
                   p.max_days_per_week as maxDaysPerWeek,
                   count(distinct rad.day) as assignedDays
            from users u
            join plans p on p.id = u.plan_id
            join routine_assignments ra on ra.user_id = u.id
                and ra.start_date <= :toDate
                and ra.end_date >= :fromDate
            join routine_assignment_days rad on rad.routine_assignment_id = ra.id
            where u.user_type = 'ALUMNO'
              and (
                :search is null
                or lower(concat(u.first_name, ' ', u.last_name)) like lower(concat('%', :search, '%'))
                or u.dni like concat('%', :search, '%')
              )
            group by u.id, u.first_name, u.last_name, u.dni, p.name, p.max_days_per_week
            having count(distinct rad.day) > p.max_days_per_week
            order by count(distinct rad.day) desc, u.last_name asc, u.first_name asc
            """,
        countQuery = """
            select count(*) from (
                select u.id
                from users u
                join plans p on p.id = u.plan_id
                join routine_assignments ra on ra.user_id = u.id
                    and ra.start_date <= :toDate
                    and ra.end_date >= :fromDate
                join routine_assignment_days rad on rad.routine_assignment_id = ra.id
                where u.user_type = 'ALUMNO'
                  and (
                    :search is null
                    or lower(concat(u.first_name, ' ', u.last_name)) like lower(concat('%', :search, '%'))
                    or u.dni like concat('%', :search, '%')
                  )
                group by u.id, p.max_days_per_week
                having count(distinct rad.day) > p.max_days_per_week
            ) overloads
            """,
        nativeQuery = true
    )
    Page<DashboardPlanOverloadProjection> findPlanOverloadsInRange(
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate,
        @Param("search") String search,
        Pageable pageable
    );

    @Query("""
        select ra
        from RoutineAssignmentEntity ra
        join ra.user u
        where (
            :student is null
            or lower(concat(u.firstName, ' ', u.lastName)) like lower(concat('%', :student, '%'))
            or u.dni like concat('%', :student, '%')
        )
          and (:templateId is null or ra.routineTemplate.id = :templateId)
          and (
            :currentOnly = false
            or (
                ra.startDate <= :referenceDate
                and ra.endDate >= :referenceDate
            )
          )
        """)
    Page<RoutineAssignmentEntity> findForTable(
        @Param("student") String student,
        @Param("templateId") Long templateId,
        @Param("currentOnly") boolean currentOnly,
        @Param("referenceDate") LocalDate referenceDate,
        Pageable pageable
    );
}
