package com.fitexa.repository;

import com.fitexa.entities.ClientEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<ClientEntity, Long> {

    Optional<ClientEntity> findBySlugIgnoreCase(String slug);
}
