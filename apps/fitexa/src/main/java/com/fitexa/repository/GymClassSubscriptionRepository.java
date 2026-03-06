package com.fitexa.repository;

import com.fitexa.entities.GymClassSubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GymClassSubscriptionRepository extends JpaRepository<GymClassSubscriptionEntity, Long> {
    long countByGymClassId(Long gymClassId);
}
