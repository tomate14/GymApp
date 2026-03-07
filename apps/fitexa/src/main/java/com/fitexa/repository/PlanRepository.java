package com.fitexa.repository;

import com.fitexa.entities.PlanEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlanRepository extends JpaRepository<PlanEntity, Long> {

    List<PlanEntity> findByActiveTrue();

    @Query("""
        select p
        from PlanEntity p
        where (:name is null or lower(p.name) like lower(concat('%', :name, '%')))
          and (:active is null or p.active = :active)
        """)
    Page<PlanEntity> findForTable(
        @Param("name") String name,
        @Param("active") Boolean active,
        Pageable pageable
    );
}
