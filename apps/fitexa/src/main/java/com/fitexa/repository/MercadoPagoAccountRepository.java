package com.fitexa.repository;

import com.fitexa.entities.MercadoPagoAccountEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MercadoPagoAccountRepository extends JpaRepository<MercadoPagoAccountEntity, Long> {
    Optional<MercadoPagoAccountEntity> findFirstByActiveTrueOrderByIdAsc();
}
