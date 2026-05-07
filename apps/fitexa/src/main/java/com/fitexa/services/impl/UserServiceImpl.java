package com.fitexa.services.impl;

import com.fitexa.dtos.UserRequestDto;
import com.fitexa.dtos.UserResponseDto;
import com.fitexa.dtos.UserRoleAssignmentRequestDto;
import com.fitexa.dtos.DashboardPageResponseDto;
import com.fitexa.entities.ActivityEntity;
import com.fitexa.entities.PaymentStatus;
import com.fitexa.entities.PlanEntity;
import com.fitexa.entities.RoleEntity;
import com.fitexa.entities.UserEntity;
import com.fitexa.entities.UserType;
import com.fitexa.exeptions.BusinessValidationException;
import com.fitexa.exeptions.ResourceNotFoundException;
import com.fitexa.repository.ActivityRepository;
import com.fitexa.repository.PlanRepository;
import com.fitexa.repository.RoleRepository;
import com.fitexa.repository.UserRepository;
import com.fitexa.services.AuthenticatedUserService;
import com.fitexa.services.UserPaymentMovementService;
import com.fitexa.services.UserService;
import com.fitexa.utils.DateUtils;
import com.fitexa.utils.PaymentStatusUtils;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final int MAX_PAGE_SIZE = 100;

    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final PlanRepository planRepository;
    private final RoleRepository roleRepository;
    private final UserPaymentMovementService userPaymentMovementService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticatedUserService authenticatedUserService;

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> findAll() {
        return userRepository.findAllByClientId(currentClientId()).stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardPageResponseDto<UserResponseDto> findPage(
        String type,
        Long roleId,
        String name,
        String dni,
        String paymentStatus,
        LocalDate registrationFrom,
        LocalDate registrationTo,
        LocalDate renewalFrom,
        LocalDate renewalTo,
        int page,
        int size
    ) {
        PaymentStatus parsedPaymentStatus = parsePaymentStatus(paymentStatus);
        Page<UserResponseDto> result = userRepository.findForTable(
            currentClientId(),
            parseUserType(type),
            roleId,
            clean(name),
            clean(dni),
            parsedPaymentStatus != null,
            parsedPaymentStatus == PaymentStatus.AL_DIA,
            LocalDate.now(),
            registrationFrom,
            registrationTo,
            renewalFrom,
            renewalTo,
            PageRequest.of(
                sanitizePage(page),
                sanitizeSize(size),
                Sort.by(Sort.Direction.DESC, "registrationDate").and(Sort.by(Sort.Direction.ASC, "lastName"))
            )
        ).map(this::toDto);

        return toPageResponse(result);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> findStudents() {
        return userRepository.findAllByClientIdAndUserType(currentClientId(), UserType.ALUMNO).stream()
            .map(this::toDto)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> findTeachers() {
        return userRepository.findAllByClientIdAndUserType(currentClientId(), UserType.PROFESOR).stream()
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
        RoleEntity role = resolveRole(request.getRoleId());
        UserType derivedUserType = resolveUserType(role);
        PlanEntity plan = resolvePlan(derivedUserType, request.getPlanId());
        Set<ActivityEntity> activities = resolveActivities(derivedUserType, request.getActivityIds());
        LocalDate registrationDate = request.getRegistrationDate() == null ? LocalDate.now() : request.getRegistrationDate();
        String normalizedEmail = normalizeEmail(request.getEmail());

        UserEntity entity = UserEntity.builder()
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .dni(request.getDni())
            .address(request.getAddress())
            .phone(request.getPhone())
            .email(normalizedEmail)
            .password(passwordEncoder.encode(normalizedEmail))
            .mustChangePassword(true)
            .birthDate(request.getBirthDate())
            .userType(derivedUserType)
            .role(role)
            .plan(plan)
            .activities(activities)
            .registrationDate(registrationDate)
            .renewalDate(DateUtils.plusOneMonth(registrationDate))
            .build();
        entity.setClient(authenticatedUserService.getCurrentClient());

        return toDto(userRepository.save(entity));
    }

    @Override
    @Transactional
    public UserResponseDto update(Long id, UserRequestDto request) {
        UserEntity entity = findEntity(id);
        RoleEntity role = resolveRole(request.getRoleId());
        UserType derivedUserType = resolveUserType(role);
        PlanEntity plan = resolvePlan(derivedUserType, request.getPlanId());
        Set<ActivityEntity> activities = resolveActivities(derivedUserType, request.getActivityIds());

        entity.setFirstName(request.getFirstName());
        entity.setLastName(request.getLastName());
        entity.setDni(request.getDni());
        entity.setAddress(request.getAddress());
        entity.setPhone(request.getPhone());
        entity.setEmail(normalizeEmail(request.getEmail()));
        entity.setBirthDate(request.getBirthDate());
        entity.setUserType(derivedUserType);
        entity.setRole(role);
        entity.setPlan(plan);
        entity.setActivities(activities);
        entity.setRegistrationDate(request.getRegistrationDate() == null ? entity.getRegistrationDate() : request.getRegistrationDate());
        String password = clean(request.getPassword());
        if (password != null) {
            entity.setPassword(passwordEncoder.encode(password));
            entity.setMustChangePassword(false);
        }

        return toDto(userRepository.save(entity));
    }

    @Override
    @Transactional
    public UserResponseDto assignRole(Long id, UserRoleAssignmentRequestDto request) {
        UserEntity entity = findEntity(id);
        RoleEntity role = resolveRole(request.getRoleId());
        UserType derivedUserType = resolveUserType(role);
        entity.setRole(role);
        entity.setUserType(derivedUserType);
        if (derivedUserType != UserType.ALUMNO) {
            entity.setPlan(null);
            entity.getActivities().clear();
        }
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
        UserEntity savedEntity = userRepository.save(entity);
        userPaymentMovementService.registerRenewalMovement(savedEntity);
        return toDto(savedEntity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        userRepository.delete(findEntity(id));
    }

    private PlanEntity resolvePlan(UserType userType, Long planId) {
        if (userType != UserType.ALUMNO) {
            return null;
        }
        if (planId == null) {
            throw new BusinessValidationException("Un alumno debe tener un plan asignado.");
        }
        return planRepository.findByIdAndClientId(planId, currentClientId())
            .orElseThrow(() -> new ResourceNotFoundException("Plan no encontrado: " + planId));
    }

    private Set<ActivityEntity> resolveActivities(UserType userType, List<Long> activityIds) {
        if (userType != UserType.ALUMNO) {
            return new LinkedHashSet<>();
        }

        List<Long> ids = activityIds == null ? List.of() : activityIds.stream()
            .filter(id -> id != null && id > 0)
            .distinct()
            .toList();

        if (ids.isEmpty()) {
            throw new BusinessValidationException("Un alumno debe tener al menos una actividad habilitada.");
        }

        List<ActivityEntity> activities = activityRepository.findAllByClientIdAndIdIn(currentClientId(), ids);
        if (activities.size() != ids.size()) {
            throw new BusinessValidationException("Una o mas actividades seleccionadas no existen.");
        }

        return new LinkedHashSet<>(activities);
    }

    private UserEntity findEntity(Long id) {
        return userRepository.findByIdAndClientId(id, currentClientId())
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));
    }

    private RoleEntity resolveRole(Long roleId) {
        if (roleId == null) {
            throw new BusinessValidationException("El rol es obligatorio.");
        }
        return roleRepository.findByIdAndClientId(roleId, currentClientId())
            .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + roleId));
    }

    private UserType resolveUserType(RoleEntity role) {
        String normalizedRoleName = role.getName().trim();
        if ("Alumno".equalsIgnoreCase(normalizedRoleName)) {
            return UserType.ALUMNO;
        }
        if ("Administrador".equalsIgnoreCase(normalizedRoleName)) {
            return UserType.ADMIN;
        }
        return UserType.PROFESOR;
    }

    private UserResponseDto toDto(UserEntity entity) {
        List<ActivityEntity> activities = entity.getActivities() == null ? List.of() : entity.getActivities().stream()
            .sorted((current, next) -> current.getName().compareToIgnoreCase(next.getName()))
            .toList();

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
            .roleId(entity.getRole() == null ? null : entity.getRole().getId())
            .roleName(entity.getRole() == null ? null : entity.getRole().getName())
            .planId(entity.getPlan() == null ? null : entity.getPlan().getId())
            .planName(entity.getPlan() == null ? null : entity.getPlan().getName())
            .planPrice(entity.getPlan() == null ? null : entity.getPlan().getPrice())
            .activityIds(activities.stream().map(ActivityEntity::getId).toList())
            .activityNames(activities.stream().map(ActivityEntity::getName).toList())
            .registrationDate(entity.getRegistrationDate())
            .renewalDate(entity.getRenewalDate())
            .paymentStatus(PaymentStatusUtils.fromRenewalDate(entity.getRenewalDate()))
            .build();
    }

    private <T> DashboardPageResponseDto<T> toPageResponse(Page<T> page) {
        return DashboardPageResponseDto.<T>builder()
            .content(page.getContent())
            .page(page.getNumber())
            .size(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .build();
    }

    private UserType parseUserType(String type) {
        if (type == null || type.isBlank()) {
            return null;
        }
        return UserType.fromValue(type.trim());
    }

    private PaymentStatus parsePaymentStatus(String paymentStatus) {
        if (paymentStatus == null || paymentStatus.isBlank()) {
            return null;
        }
        return PaymentStatus.fromValue(paymentStatus.trim());
    }

    private String clean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private int sanitizePage(int page) {
        return Math.max(0, page);
    }

    private int sanitizeSize(int size) {
        if (size <= 0) {
            return 8;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }

    private String normalizeEmail(String value) {
        String cleanValue = clean(value);
        return cleanValue == null ? null : cleanValue.toLowerCase();
    }

    private Long currentClientId() {
        return authenticatedUserService.getCurrentClientId();
    }
}
