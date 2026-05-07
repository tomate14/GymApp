package com.fitexa.repository;

import com.fitexa.entities.RoutineAssignmentExerciseEntity;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoutineAssignmentExerciseRepository extends JpaRepository<RoutineAssignmentExerciseEntity, Long> {
    @Query("""
        select item.exercise.id as exerciseId,
               item.exerciseName as exerciseName,
               count(item.id) as usageCount
        from RoutineAssignmentExerciseEntity item
        where item.client.id = :clientId
        group by item.exercise.id, item.exerciseName
        order by count(item.id) desc
        """)
    List<DashboardExerciseUsageProjection> findTopExercisesUsage(@Param("clientId") Long clientId, Pageable pageable);

    @Query("""
        select item.exercise.id as exerciseId,
               item.exerciseName as exerciseName,
               count(item.id) as usageCount
        from RoutineAssignmentExerciseEntity item
        where item.client.id = :clientId
          and item.assignment.startDate <= :toDate
          and item.assignment.endDate >= :fromDate
        group by item.exercise.id, item.exerciseName
        order by count(item.id) desc
        """)
    List<DashboardExerciseUsageProjection> findTopExercisesUsageInRange(
        @Param("clientId") Long clientId,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate,
        Pageable pageable
    );

    @Query("""
        select item.exercise.machine.id as machineId,
               item.exercise.machine.name as machineName,
               count(item.id) as usageCount
        from RoutineAssignmentExerciseEntity item
        where item.client.id = :clientId
          and item.exercise.machine is not null
        group by item.exercise.machine.id, item.exercise.machine.name
        order by count(item.id) desc
        """)
    List<DashboardMachineUsageProjection> findTopMachinesUsage(@Param("clientId") Long clientId, Pageable pageable);

    @Query(
        value = """
            with recursive routine_days as (
                select ra.id as assignment_id,
                       greatest(ra.start_date, :fromDate) as target_date,
                       least(ra.end_date, :toDate) as end_date
                from routine_assignments ra
                where ra.start_date <= :toDate
                  and ra.client_id = :clientId
                  and ra.end_date >= :fromDate

                union all

                select rd.assignment_id,
                       date_add(rd.target_date, interval 1 day) as target_date,
                       rd.end_date
                from routine_days rd
                where rd.target_date < rd.end_date
            )
            select machine_stats.machine_id as machineId,
                   machine_stats.machine_name as machineName,
                   round(count(*) / machine_stats.machine_quantity, 2) as usageCount
            from (
                select distinct
                    rd.assignment_id,
                    rd.target_date,
                    m.id as machine_id,
                    m.name as machine_name,
                    greatest(m.quantity, 1) as machine_quantity
                from routine_days rd
                join routine_assignment_days rad
                    on rad.routine_assignment_id = rd.assignment_id
                join routine_assignment_exercises rae
                    on rae.assignment_id = rd.assignment_id
                join exercises e
                    on e.id = rae.exercise_id
                join machines m
                    on m.id = e.machine_id
                where e.machine_id is not null
                  and rad.day = case dayofweek(rd.target_date)
                      when 1 then 'DOMINGO'
                      when 2 then 'LUNES'
                      when 3 then 'MARTES'
                      when 4 then 'MIERCOLES'
                      when 5 then 'JUEVES'
                      when 6 then 'VIERNES'
                      when 7 then 'SABADO'
                  end
            ) machine_stats
            group by machine_stats.machine_id, machine_stats.machine_name, machine_stats.machine_quantity
            order by usageCount desc, machine_stats.machine_name asc
            limit 10
            """,
        nativeQuery = true
    )
    List<DashboardMachineUsageProjection> findTopMachinesUsageByCompletedRoutineDays(
        @Param("clientId") Long clientId,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );
}
