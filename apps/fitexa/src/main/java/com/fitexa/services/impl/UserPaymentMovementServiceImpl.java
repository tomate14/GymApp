package com.fitexa.services.impl;

import com.fitexa.dtos.UserPaymentMovementResponseDto;
import com.fitexa.entities.UserEntity;
import com.fitexa.entities.UserPaymentMovementEntity;
import com.fitexa.entities.UserType;
import com.fitexa.exeptions.BusinessValidationException;
import com.fitexa.exeptions.ResourceNotFoundException;
import com.fitexa.repository.UserPaymentMovementRepository;
import com.fitexa.repository.UserRepository;
import com.fitexa.services.UserPaymentMovementService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserPaymentMovementServiceImpl implements UserPaymentMovementService {

    private static final String RENEWAL_DETAIL = "Renovacion de cuota";

    private final UserPaymentMovementRepository userPaymentMovementRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UserPaymentMovementResponseDto> findByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Usuario no encontrado: " + userId);
        }
        return userPaymentMovementRepository.findByUserIdOrderByPaidAtDesc(userId).stream().map(this::toDto).toList();
    }

    @Override
    @Transactional
    public void registerRenewalMovement(UserEntity user) {
        if (user == null || user.getId() == null) {
            throw new BusinessValidationException("No se pudo registrar el pago: usuario invalido.");
        }
        if (user.getUserType() != UserType.ALUMNO) {
            throw new BusinessValidationException("Solo los alumnos pueden registrar movimientos de pago.");
        }
        if (user.getPlan() == null) {
            throw new BusinessValidationException("No se pudo registrar el pago: el alumno no tiene plan asignado.");
        }

        UserPaymentMovementEntity entity = UserPaymentMovementEntity.builder()
            .user(user)
            .plan(user.getPlan())
            .amount(user.getPlan().getPrice())
            .detail(RENEWAL_DETAIL)
            .paidAt(LocalDateTime.now())
            .build();

        userPaymentMovementRepository.save(entity);
    }

    private UserPaymentMovementResponseDto toDto(UserPaymentMovementEntity entity) {
        return UserPaymentMovementResponseDto.builder()
            .id(entity.getId())
            .userId(entity.getUser().getId())
            .studentName(entity.getUser().getFirstName() + " " + entity.getUser().getLastName())
            .planId(entity.getPlan() == null ? null : entity.getPlan().getId())
            .planName(entity.getPlan() == null ? "Sin plan" : entity.getPlan().getName())
            .amount(entity.getAmount())
            .detail(entity.getDetail())
            .paidAt(entity.getPaidAt())
            .build();
    }
}
