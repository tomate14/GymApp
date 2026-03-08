package com.fitexa.services;

import com.fitexa.dtos.UserPaymentMovementResponseDto;
import com.fitexa.entities.UserEntity;
import java.util.List;

public interface UserPaymentMovementService {
    List<UserPaymentMovementResponseDto> findByUserId(Long userId);
    void registerRenewalMovement(UserEntity user);
}
