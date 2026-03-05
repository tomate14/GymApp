package com.fitexa.services.impl;

import com.fitexa.dtos.UserRequestDto;
import com.fitexa.dtos.UserResponseDto;
import com.fitexa.entities.PaymentStatus;
import com.fitexa.entities.PlanEntity;
import com.fitexa.entities.UserEntity;
import com.fitexa.entities.UserType;
import com.fitexa.exeptions.BusinessValidationException;
import com.fitexa.exeptions.ResourceNotFoundException;
import com.fitexa.repository.PlanRepository;
import com.fitexa.repository.UserRepository;
import com.fitexa.services.UserService;
import com.fitexa.utils.DateUtils;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PlanRepository planRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> findAll() {
        return userRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> findStudents() {
        return userRepository.findAll().stream()
            .filter(user -> user.getUserType() == UserType.ALUMNO)
            .map(this::toDto)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> findTeachers() {
        return userRepository.findAll().stream()
            .filter(user -> user.getUserType() == UserType.PROFESOR)
            .map(this::toDto)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto findById(Long id) {
        return toDto(findEntity(id));
    }

    @Override
    @Transactional
    public UserResponseDto create(UserRequestDto request) {
        PlanEntity plan = resolvePlan(request.getUserType(), request.getPlanId());
        LocalDate registrationDate = request.getRegistrationDate() == null ? LocalDate.now() : request.getRegistrationDate();

        UserEntity entity = UserEntity.builder()
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .dni(request.getDni())
            .address(request.getAddress())
            .phone(request.getPhone())
            .email(request.getEmail())
            .birthDate(request.getBirthDate())
            .userType(request.getUserType())
            .plan(plan)
            .registrationDate(registrationDate)
            .renewalDate(DateUtils.plusOneMonth(registrationDate))
            .paymentStatus(request.getPaymentStatus())
            .build();

        return toDto(userRepository.save(entity));
    }

    @Override
    @Transactional
    public UserResponseDto update(Long id, UserRequestDto request) {
        UserEntity entity = findEntity(id);
        PlanEntity plan = resolvePlan(request.getUserType(), request.getPlanId());

        entity.setFirstName(request.getFirstName());
        entity.setLastName(request.getLastName());
        entity.setDni(request.getDni());
        entity.setAddress(request.getAddress());
        entity.setPhone(request.getPhone());
        entity.setEmail(request.getEmail());
        entity.setBirthDate(request.getBirthDate());
        entity.setUserType(request.getUserType());
        entity.setPlan(plan);
        entity.setRegistrationDate(request.getRegistrationDate() == null ? entity.getRegistrationDate() : request.getRegistrationDate());
        entity.setPaymentStatus(request.getPaymentStatus());

        return toDto(userRepository.save(entity));
    }

    @Override
    @Transactional
    public UserResponseDto renewQuota(Long id) {
        UserEntity entity = findEntity(id);
        if (entity.getUserType() != UserType.ALUMNO) {
            throw new BusinessValidationException("Solo los alumnos pueden renovar cuota.");
        }
        if (entity.getPlan() == null) {
            throw new BusinessValidationException("El alumno no tiene plan asignado.");
        }

        entity.setRenewalDate(DateUtils.plusOneMonth(LocalDate.now()));
        entity.setPaymentStatus(PaymentStatus.AL_DIA);
        return toDto(userRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        userRepository.delete(findEntity(id));
    }

    private PlanEntity resolvePlan(UserType userType, Long planId) {
        if (userType == UserType.PROFESOR) {
            return null;
        }
        if (planId == null) {
            throw new BusinessValidationException("Un alumno debe tener un plan asignado.");
        }
        return planRepository.findById(planId)
            .orElseThrow(() -> new ResourceNotFoundException("Plan no encontrado: " + planId));
    }

    private UserEntity findEntity(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));
    }

    private UserResponseDto toDto(UserEntity entity) {
        return UserResponseDto.builder()
            .id(entity.getId())
            .firstName(entity.getFirstName())
            .lastName(entity.getLastName())
            .dni(entity.getDni())
            .address(entity.getAddress())
            .phone(entity.getPhone())
            .email(entity.getEmail())
            .birthDate(entity.getBirthDate())
            .userType(entity.getUserType())
            .planId(entity.getPlan() == null ? null : entity.getPlan().getId())
            .planName(entity.getPlan() == null ? null : entity.getPlan().getName())
            .planPrice(entity.getPlan() == null ? null : entity.getPlan().getPrice())
            .registrationDate(entity.getRegistrationDate())
            .renewalDate(entity.getRenewalDate())
            .paymentStatus(entity.getPaymentStatus())
            .build();
    }
}
