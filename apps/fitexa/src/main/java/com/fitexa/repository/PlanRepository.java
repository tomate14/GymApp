package com.fitexa.repository;

import com.fitexa.entities.PlanEntity;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlanRepository extends JpaRepository<PlanEntity, Long> {

    List<PlanEntity> findByClientId(Long clientId);
    List<PlanEntity> findByClientIdAndActiveTrue(Long clientId);
    java.util.Optional<PlanEntity> findByIdAndClientId(Long id, Long clientId);

    @Query("""
        select p
        from PlanEntity p
        where p.client.id = :clientId
          and (:name is null or lower(p.name) like lower(concat('%', :name, '%')))
          and (:active is null or p.active = :active)
        """)
    Page<PlanEntity> findForTable(
        @Param("clientId") Long clientId,
        @Param("name") String name,
        @Param("active") Boolean active,
        Pageable pageable
    );
}
