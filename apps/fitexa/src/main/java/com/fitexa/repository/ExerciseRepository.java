package com.fitexa.repository;

import com.fitexa.entities.ExerciseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExerciseRepository extends JpaRepository<ExerciseEntity, Long> {

    @Query("""
        select e
        from ExerciseEntity e
        where (:name is null or lower(e.name) like lower(concat('%', :name, '%')))
          and (:activityId is null or e.activity.id = :activityId)
          and (:machineId is null or e.machine.id = :machineId)
        """)
    Page<ExerciseEntity> findForTable(
        @Param("name") String name,
        @Param("activityId") Long activityId,
        @Param("machineId") Long machineId,
        Pageable pageable
    );
}
