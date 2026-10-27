package com.fitexa.repository;

import com.fitexa.entities.ClientSubscriptionEntity;
import com.fitexa.entities.ClientSubscriptionStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientSubscriptionRepository extends JpaRepository<ClientSubscriptionEntity, Long> {
    Optional<ClientSubscriptionEntity> findByClientId(Long clientId);
    Optional<ClientSubscriptionEntity> findByMercadoPagoPreapprovalId(String mercadoPagoPreapprovalId);
    List<ClientSubscriptionEntity> findByStatusInAndMercadoPagoPreapprovalIdIsNotNull(List<ClientSubscriptionStatus> statuses);
}
