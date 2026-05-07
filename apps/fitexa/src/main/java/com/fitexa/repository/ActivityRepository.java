package com.fitexa.repository;

import com.fitexa.entities.ActivityEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ActivityRepository extends JpaRepository<ActivityEntity, Long> {
    java.util.Optional<ActivityEntity> findByIdAndClientId(Long id, Long clientId);
    java.util.List<ActivityEntity> findAllByClientId(Long clientId);
    java.util.List<ActivityEntity> findAllByClientIdAndIdIn(Long clientId, java.util.Collection<Long> ids);

    @Query("""
        select a
        from ActivityEntity a
        where a.client.id = :clientId
          and (:name is null or lower(a.name) like lower(concat('%', :name, '%')))
          and (:description is null or lower(a.description) like lower(concat('%', :description, '%')))
        """)
    Page<ActivityEntity> findForTable(
        @Param("clientId") Long clientId,
        @Param("name") String name,
        @Param("description") String description,
        Pageable pageable
    );
}
