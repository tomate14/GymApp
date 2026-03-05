package com.fitexa.repository;

import com.fitexa.entities.RoutineAssignmentEntity;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoutineAssignmentRepository extends JpaRepository<RoutineAssignmentEntity, Long> {

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
}
