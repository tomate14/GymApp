package com.fitexa.repository;

import com.fitexa.entities.MachineEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MachineRepository extends JpaRepository<MachineEntity, Long> {

    @Query("""
        select m
        from MachineEntity m
        where (:name is null or lower(m.name) like lower(concat('%', :name, '%')))
          and (:onlyInUse = false or exists (
            select 1 from ExerciseEntity e where e.machine.id = m.id
          ))
        """)
    Page<MachineEntity> findForTable(
        @Param("name") String name,
        @Param("onlyInUse") boolean onlyInUse,
        Pageable pageable
    );
}
