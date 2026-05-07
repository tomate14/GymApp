package com.fitexa.repository;

import com.fitexa.entities.RoleEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    Optional<RoleEntity> findByNameIgnoreCaseAndClientId(String name, Long clientId);

    boolean existsByNameIgnoreCaseAndClientId(String name, Long clientId);
    Optional<RoleEntity> findByIdAndClientId(Long id, Long clientId);
    List<RoleEntity> findAllByClientId(Long clientId);
}
