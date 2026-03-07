package com.fitexa.repository;

import com.fitexa.entities.RoutineTemplateEntity;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoutineTemplateRepository extends JpaRepository<RoutineTemplateEntity, Long> {

    @Query("""
        select rt
        from RoutineTemplateEntity rt
        where (:name is null or lower(rt.name) like lower(concat('%', :name, '%')))
          and (:minExercises is null or size(rt.exercises) >= :minExercises)
          and (:minStudents is null or (
            select count(ra.id)
            from RoutineAssignmentEntity ra
            where ra.routineTemplate.id = rt.id
              and ra.startDate <= :referenceDate
              and ra.endDate >= :referenceDate
          ) >= :minStudents)
        """)
    Page<RoutineTemplateEntity> findForTable(
        @Param("name") String name,
        @Param("minExercises") Integer minExercises,
        @Param("minStudents") Integer minStudents,
        @Param("referenceDate") LocalDate referenceDate,
        Pageable pageable
    );
}
