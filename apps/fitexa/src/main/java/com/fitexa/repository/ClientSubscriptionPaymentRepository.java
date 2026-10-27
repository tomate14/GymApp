package com.fitexa.repository;

import com.fitexa.entities.ClientSubscriptionPaymentEntity;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientSubscriptionPaymentRepository extends JpaRepository<ClientSubscriptionPaymentEntity, Long> {
    Optional<ClientSubscriptionPaymentEntity> findByMercadoPagoAuthorizedPaymentId(String mercadoPagoAuthorizedPaymentId);
    Optional<ClientSubscriptionPaymentEntity> findByMercadoPagoPaymentId(String mercadoPagoPaymentId);
    boolean existsByClientIdAndPeriodEndGreaterThanEqual(Long clientId, LocalDateTime referenceDate);
}
