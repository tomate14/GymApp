package com.fitexa.repository;

import com.fitexa.entities.BillingPlanEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillingPlanRepository extends JpaRepository<BillingPlanEntity, Long> {
    List<BillingPlanEntity> findByActiveTrueOrderByPriceAsc();
    Optional<BillingPlanEntity> findByIdAndActiveTrue(Long id);
    Optional<BillingPlanEntity> findByMercadoPagoPlanId(String mercadoPagoPlanId);
}
