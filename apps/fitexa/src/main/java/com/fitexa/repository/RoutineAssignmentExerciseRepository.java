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
        group by item.exercise.id, item.exerciseName
        order by count(item.id) desc
        """)
    List<DashboardExerciseUsageProjection> findTopExercisesUsage(Pageable pageable);

    @Query("""
        select item.exercise.id as exerciseId,
               item.exerciseName as exerciseName,
               count(item.id) as usageCount
        from RoutineAssignmentExerciseEntity item
        where item.assignment.startDate <= :toDate
          and item.assignment.endDate >= :fromDate
        group by item.exercise.id, item.exerciseName
        order by count(item.id) desc
        """)
    List<DashboardExerciseUsageProjection> findTopExercisesUsageInRange(
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate,
        Pageable pageable
    );

    @Query("""
        select item.exercise.machine.id as machineId,
               item.exercise.machine.name as machineName,
               count(item.id) as usageCount
        from RoutineAssignmentExerciseEntity item
        where item.exercise.machine is not null
        group by item.exercise.machine.id, item.exercise.machine.name
        order by count(item.id) desc
        """)
    List<DashboardMachineUsageProjection> findTopMachinesUsage(Pageable pageable);

    @Query("""
        select item.exercise.machine.id as machineId,
               item.exercise.machine.name as machineName,
               count(item.id) as usageCount
        from RoutineAssignmentExerciseEntity item
        where item.exercise.machine is not null
          and item.assignment.startDate <= :toDate
          and item.assignment.endDate >= :fromDate
        group by item.exercise.machine.id, item.exercise.machine.name
        order by count(item.id) desc
        """)
    List<DashboardMachineUsageProjection> findTopMachinesUsageInRange(
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate,
        Pageable pageable
    );
}
