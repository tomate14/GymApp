package com.fitexa.repository;

import com.fitexa.entities.RoleEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    Optional<RoleEntity> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
