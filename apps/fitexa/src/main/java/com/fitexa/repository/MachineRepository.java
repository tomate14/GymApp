package com.fitexa.repository;

import com.fitexa.entities.MachineEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MachineRepository extends JpaRepository<MachineEntity, Long> {
    java.util.Optional<MachineEntity> findByIdAndClientId(Long id, Long clientId);
    java.util.List<MachineEntity> findAllByClientId(Long clientId);

    @Query("""
        select m
        from MachineEntity m
        where m.client.id = :clientId
          and (:name is null or lower(m.name) like lower(concat('%', :name, '%')))
          and (:onlyInUse = false or exists (
            select 1 from ExerciseEntity e where e.machine.id = m.id and e.client.id = :clientId
          ))
        """)
    Page<MachineEntity> findForTable(
        @Param("clientId") Long clientId,
        @Param("name") String name,
        @Param("onlyInUse") boolean onlyInUse,
        Pageable pageable
    );
}
