package com.fitexa.repository;

import com.fitexa.entities.BillingEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillingEventRepository extends JpaRepository<BillingEventEntity, Long> {
    boolean existsByEventKey(String eventKey);
}
