package com.fitexa.repository;

import com.fitexa.entities.GymClassEntity;
import com.fitexa.entities.WeekDay;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GymClassRepository extends JpaRepository<GymClassEntity, Long>, JpaSpecificationExecutor<GymClassEntity> {
    @Query("""
        select min(gc.date)
        from GymClassEntity gc
        where gc.client.id = :clientId
        """)
    LocalDate findEarliestClassDate(@Param("clientId") Long clientId);

    java.util.Optional<GymClassEntity> findByIdAndClientId(Long id, Long clientId);
    java.util.List<GymClassEntity> findAllByClientId(Long clientId);

    @Query("""
        select gc
        from GymClassEntity gc
        where gc.client.id = :clientId
          and :weekDay in elements(gc.weekDays)
        """)
    List<GymClassEntity> findByWeekDayAndClientId(@Param("weekDay") WeekDay weekDay, @Param("clientId") Long clientId);

    long countByClientIdAndDate(Long clientId, LocalDate date);

    long countByClientIdAndDateBetween(Long clientId, LocalDate fromDate, LocalDate toDate);

    long countByClientIdAndTeacherIsNullAndDateGreaterThanEqual(Long clientId, LocalDate date);

    long countByClientIdAndTeacherIsNullAndDateBetween(Long clientId, LocalDate fromDate, LocalDate toDate);

    @Query("""
        select count(gc.id)
        from GymClassEntity gc
        where gc.client.id = :clientId
          and gc.maxCapacity is not null
          and gc.maxCapacity > 0
          and gc.enrolled >= gc.maxCapacity
          and gc.date >= :referenceDate
        """)
    long countAtOrAboveCapacity(@Param("clientId") Long clientId, @Param("referenceDate") LocalDate referenceDate);

    @Query("""
        select count(gc.id)
        from GymClassEntity gc
        where gc.client.id = :clientId
          and gc.maxCapacity is not null
          and gc.maxCapacity > 0
          and gc.enrolled >= gc.maxCapacity
          and gc.date >= :fromDate
          and gc.date <= :toDate
        """)
    long countAtOrAboveCapacityInRange(
        @Param("clientId") Long clientId,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );

    @Query("""
        select avg((gc.enrolled * 100.0) / gc.maxCapacity)
        from GymClassEntity gc
        where gc.client.id = :clientId
          and gc.maxCapacity is not null
          and gc.maxCapacity > 0
          and (:fromDate is null or gc.date >= :fromDate)
          and (:toDate is null or gc.date <= :toDate)
        """)
    Double averageOccupancyPercentage(
        @Param("clientId") Long clientId,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );

    @Query("""
        select coalesce(a.name, 'Sin actividad'), count(gc.id)
        from GymClassEntity gc
        left join gc.activity a
        where gc.client.id = :clientId
          and (:fromDate is null or gc.date >= :fromDate)
          and (:toDate is null or gc.date <= :toDate)
        group by a.name
        order by count(gc.id) desc
        """)
    List<Object[]> countClassesByActivity(
        @Param("clientId") Long clientId,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );

    @Query("""
        select coalesce(a.name, 'Sin actividad'),
               avg(case when gc.maxCapacity is null or gc.maxCapacity = 0 then 0 else ((gc.enrolled * 100.0) / gc.maxCapacity) end)
        from GymClassEntity gc
        left join gc.activity a
        where gc.client.id = :clientId
          and (:fromDate is null or gc.date >= :fromDate)
          and (:toDate is null or gc.date <= :toDate)
        group by a.name
        order by avg(case when gc.maxCapacity is null or gc.maxCapacity = 0 then 0 else ((gc.enrolled * 100.0) / gc.maxCapacity) end) desc
        """)
    List<Object[]> averageOccupancyByActivity(
        @Param("clientId") Long clientId,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );

    @Query("""
        select a.id as activityId,
               coalesce(a.name, 'Sin actividad') as activityName,
               count(gc.id) as classesCount,
               coalesce(sum(gc.enrolled), 0) as totalEnrolled,
               avg(case when gc.maxCapacity is null or gc.maxCapacity = 0 then 0 else ((gc.enrolled * 100.0) / gc.maxCapacity) end) as averageOccupancyPercent
        from GymClassEntity gc
        left join gc.activity a
        where gc.client.id = :clientId
          and (:fromDate is null or gc.date >= :fromDate)
          and (:toDate is null or gc.date <= :toDate)
          and (:search is null or lower(coalesce(a.name, 'Sin actividad')) like lower(concat('%', :search, '%')))
        group by a.id, a.name
        order by count(gc.id) desc, coalesce(sum(gc.enrolled), 0) desc
        """)
    Page<DashboardActivityRankingProjection> findActivityRanking(
        @Param("clientId") Long clientId,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate,
        @Param("search") String search,
        Pageable pageable
    );

    @Query("""
        select gc
        from GymClassEntity gc
        where gc.client.id = :clientId
          and (:className is null or lower(gc.className) like lower(concat('%', :className, '%')))
          and (:activityId is null or gc.activity.id = :activityId)
          and (:teacherId is null or gc.teacher.id = :teacherId)
          and (:weekDay is null or :weekDay in elements(gc.weekDays))
        """)
    Page<GymClassEntity> findForTable(
        @Param("clientId") Long clientId,
        @Param("className") String className,
        @Param("activityId") Long activityId,
        @Param("teacherId") Long teacherId,
        @Param("weekDay") WeekDay weekDay,
        Pageable pageable
    );
}
