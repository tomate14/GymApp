package com.fitexa.repository;

import com.fitexa.entities.UserPaymentMovementEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPaymentMovementRepository extends JpaRepository<UserPaymentMovementEntity, Long> {
    List<UserPaymentMovementEntity> findByUserIdOrderByPaidAtDesc(Long userId);
    Optional<UserPaymentMovementEntity> findByIdAndUserId(Long id, Long userId);
}
