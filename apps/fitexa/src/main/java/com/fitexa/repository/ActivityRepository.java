package com.fitexa.repository;

import com.fitexa.entities.ActivityEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ActivityRepository extends JpaRepository<ActivityEntity, Long> {

    @Query("""
        select a
        from ActivityEntity a
        where (:name is null or lower(a.name) like lower(concat('%', :name, '%')))
          and (:description is null or lower(a.description) like lower(concat('%', :description, '%')))
        """)
    Page<ActivityEntity> findForTable(
        @Param("name") String name,
        @Param("description") String description,
        Pageable pageable
    );
}
