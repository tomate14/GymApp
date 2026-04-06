package com.fitexa.services.impl;

import com.fitexa.dtos.mobile.MobileGymClassResponseDto;
import com.fitexa.dtos.mobile.MobilePaymentMovementResponseDto;
import com.fitexa.dtos.mobile.MobilePersonalRecordDetailResponseDto;
import com.fitexa.dtos.mobile.MobilePersonalRecordHistoryPointResponseDto;
import com.fitexa.dtos.mobile.MobilePersonalRecordRequestDto;
import com.fitexa.dtos.mobile.MobilePersonalRecordResponseDto;
import com.fitexa.dtos.mobile.MobileProfileResponseDto;
import com.fitexa.dtos.mobile.MobileProfileUpdateRequestDto;
import com.fitexa.dtos.mobile.MobileRoutineExerciseResponseDto;
import com.fitexa.dtos.mobile.MobileRoutineResponseDto;
import com.fitexa.entities.ExerciseEntity;
import com.fitexa.entities.ExercisePersonalRecordEntryEntity;
import com.fitexa.entities.ExercisePersonalRecordEntity;
import com.fitexa.entities.GymClassEntity;
import com.fitexa.entities.GymClassSubscriptionEntity;
import com.fitexa.entities.PaymentStatus;
import com.fitexa.entities.RoutineAssignmentEntity;
import com.fitexa.entities.RoutineAssignmentExerciseEntity;
import com.fitexa.entities.UserEntity;
import com.fitexa.entities.UserPaymentMovementEntity;
import com.fitexa.entities.WeekDay;
import com.fitexa.exeptions.BusinessValidationException;
import com.fitexa.exeptions.ResourceNotFoundException;
import com.fitexa.repository.ExercisePersonalRecordRepository;
import com.fitexa.repository.ExercisePersonalRecordEntryRepository;
import com.fitexa.repository.ExerciseRepository;
import com.fitexa.repository.GymClassRepository;
import com.fitexa.repository.GymClassSubscriptionRepository;
import com.fitexa.repository.RoutineAssignmentRepository;
import com.fitexa.repository.UserPaymentMovementRepository;
import com.fitexa.services.AuthenticatedUserService;
import com.fitexa.services.MobileAppService;
import com.fitexa.services.PaymentReceiptPdfService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MobileAppServiceImpl implements MobileAppService {

    private static final String PLAN_PAYMENT_RESTRICTION_MESSAGE =
        "Tu plan esta vencido o adeudado. Regulariza tu cuota para anotarte a clases.";

    private final AuthenticatedUserService authenticatedUserService;
    private final RoutineAssignmentRepository routineAssignmentRepository;
    private final ExerciseRepository exerciseRepository;
    private final ExercisePersonalRecordRepository exercisePersonalRecordRepository;
    private final ExercisePersonalRecordEntryRepository exercisePersonalRecordEntryRepository;
    private final GymClassRepository gymClassRepository;
    private final GymClassSubscriptionRepository gymClassSubscriptionRepository;
    private final UserPaymentMovementRepository userPaymentMovementRepository;
    private final PaymentReceiptPdfService paymentReceiptPdfService;

    @Override
    @Transactional(readOnly = true)
    public MobileProfileResponseDto getMyProfile() {
        UserEntity user = authenticatedUserService.getCurrentUser();

        return toMobileProfileDto(user);
    }

    @Override
    @Transactional
    public MobileProfileResponseDto updateMyProfile(MobileProfileUpdateRequestDto request) {
        UserEntity user = authenticatedUserService.getCurrentUser();
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setPhone(request.getPhone().trim());
        user.setAddress(request.getAddress().trim());
        user.setBirthDate(request.getBirthDate());
        return toMobileProfileDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MobileRoutineResponseDto> getMyRoutines() {
        UserEntity user = authenticatedUserService.getCurrentUser();
        LocalDate today = LocalDate.now();

        return routineAssignmentRepository.findByUserIdOrderByStartDateDescIdDesc(user.getId()).stream()
            .sorted(
                Comparator.comparing((RoutineAssignmentEntity item) -> routinePriority(item, today))
                    .thenComparing(RoutineAssignmentEntity::getStartDate, Comparator.reverseOrder())
            )
            .map(item -> toRoutineDto(item, today))
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MobilePaymentMovementResponseDto> getMyPaymentMovements() {
        UserEntity user = authenticatedUserService.getCurrentUser();
        return userPaymentMovementRepository.findByUserIdOrderByPaidAtDesc(user.getId()).stream()
            .map(this::toMobilePaymentDto)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadMyPaymentMovementPdf(Long movementId) {
        UserEntity user = authenticatedUserService.getCurrentUser();
        UserPaymentMovementEntity movement = userPaymentMovementRepository.findByIdAndUserId(movementId, user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Movimiento de pago no encontrado: " + movementId));
        return paymentReceiptPdfService.generateReceipt(movement);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MobilePersonalRecordResponseDto> getMyPersonalRecords() {
        UserEntity user = authenticatedUserService.getCurrentUser();
        Map<Long, MobilePersonalRecordResponseDto> items = new LinkedHashMap<>();

        for (RoutineAssignmentEntity assignment : routineAssignmentRepository.findByUserIdOrderByStartDateDescIdDesc(user.getId())) {
            for (RoutineAssignmentExerciseEntity exercisePlan : assignment.getExercisePlans()) {
                ExerciseEntity exercise = exercisePlan.getExercise();
                items.putIfAbsent(
                    exercise.getId(),
                    MobilePersonalRecordResponseDto.builder()
                        .exerciseId(exercise.getId())
                        .exerciseName(exercise.getName())
                        .activityName(exercise.getActivity() == null ? "Sin actividad" : exercise.getActivity().getName())
                        .maxWeight(null)
                        .recordedAt(null)
                        .build()
                );
            }
        }

        for (ExercisePersonalRecordEntity record : exercisePersonalRecordRepository.findByUserId(user.getId())) {
            ExerciseEntity exercise = record.getExercise();
            items.put(
                exercise.getId(),
                MobilePersonalRecordResponseDto.builder()
                    .exerciseId(exercise.getId())
                    .exerciseName(exercise.getName())
                    .activityName(exercise.getActivity() == null ? "Sin actividad" : exercise.getActivity().getName())
                    .maxWeight(record.getMaxWeight())
                    .recordedAt(record.getRecordedAt())
                    .build()
            );
        }

        return items.values().stream()
            .sorted(Comparator.comparing(MobilePersonalRecordResponseDto::getExerciseName, String.CASE_INSENSITIVE_ORDER))
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MobilePersonalRecordDetailResponseDto getMyPersonalRecordDetail(Long exerciseId) {
        UserEntity user = authenticatedUserService.getCurrentUser();
        ensureExerciseWorkedByUser(user.getId(), exerciseId);

        ExerciseEntity exercise = exerciseRepository.findById(exerciseId)
            .orElseThrow(() -> new ResourceNotFoundException("Ejercicio no encontrado: " + exerciseId));

        ExercisePersonalRecordEntity summary = exercisePersonalRecordRepository.findByUserIdAndExerciseId(user.getId(), exerciseId)
            .orElse(null);

        List<MobilePersonalRecordHistoryPointResponseDto> history = exercisePersonalRecordEntryRepository
            .findByUserIdAndExerciseIdOrderByRecordedAtAscIdAsc(user.getId(), exerciseId).stream()
            .map(item -> MobilePersonalRecordHistoryPointResponseDto.builder()
                .id(item.getId())
                .weight(item.getWeight())
                .recordedAt(item.getRecordedAt())
                .build())
            .toList();

        return MobilePersonalRecordDetailResponseDto.builder()
            .exerciseId(exercise.getId())
            .exerciseName(exercise.getName())
            .activityName(exercise.getActivity() == null ? "Sin actividad" : exercise.getActivity().getName())
            .maxWeight(summary == null ? null : summary.getMaxWeight())
            .recordedAt(summary == null ? null : summary.getRecordedAt())
            .history(history)
            .build();
    }

    @Override
    @Transactional
    public MobilePersonalRecordResponseDto savePersonalRecord(Long exerciseId, MobilePersonalRecordRequestDto request) {
        UserEntity user = authenticatedUserService.getCurrentUser();
        ensureExerciseWorkedByUser(user.getId(), exerciseId);

        ExerciseEntity exercise = exerciseRepository.findById(exerciseId)
            .orElseThrow(() -> new ResourceNotFoundException("Ejercicio no encontrado: " + exerciseId));

        ExercisePersonalRecordEntity entity = exercisePersonalRecordRepository.findByUserIdAndExerciseId(user.getId(), exerciseId)
            .orElseGet(() -> ExercisePersonalRecordEntity.builder().user(user).exercise(exercise).build());

        exercisePersonalRecordEntryRepository.save(
            ExercisePersonalRecordEntryEntity.builder()
                .user(user)
                .exercise(exercise)
                .weight(request.getMaxWeight())
                .recordedAt(request.getRecordedAt())
                .build()
        );

        if (entity.getId() == null || entity.getMaxWeight() == null || request.getMaxWeight() >= entity.getMaxWeight()) {
            entity.setMaxWeight(request.getMaxWeight());
            entity.setRecordedAt(request.getRecordedAt());
        }

        ExercisePersonalRecordEntity saved = exercisePersonalRecordRepository.save(entity);
        return MobilePersonalRecordResponseDto.builder()
            .exerciseId(saved.getExercise().getId())
            .exerciseName(saved.getExercise().getName())
            .activityName(saved.getExercise().getActivity() == null ? "Sin actividad" : saved.getExercise().getActivity().getName())
            .maxWeight(saved.getMaxWeight())
            .recordedAt(saved.getRecordedAt())
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MobileGymClassResponseDto> getClassesForDate(LocalDate date) {
        UserEntity user = authenticatedUserService.getCurrentUser();
        LocalDate targetDate = normalizeTargetDate(date);
        WeekDay targetWeekDay = toWeekDay(targetDate);
        Set<Long> subscribedIds = gymClassSubscriptionRepository.findByUserIdAndTargetDate(user.getId(), targetDate).stream()
            .map(item -> item.getGymClass().getId())
            .collect(java.util.stream.Collectors.toSet());

        return gymClassRepository.findByWeekDay(targetWeekDay).stream()
            .map(item -> toMobileClassDto(item, user, targetDate, subscribedIds.contains(item.getId())))
            .sorted(
                Comparator.comparing((MobileGymClassResponseDto item) -> !Boolean.TRUE.equals(item.getAllDay()))
                    .thenComparing(item -> item.getStartTime() == null ? java.time.LocalTime.MIN : item.getStartTime())
                    .thenComparing(MobileGymClassResponseDto::getClassName, String.CASE_INSENSITIVE_ORDER)
            )
            .toList();
    }

    @Override
    @Transactional
    public MobileGymClassResponseDto subscribeToClass(Long classId, LocalDate date) {
        UserEntity user = authenticatedUserService.getCurrentUser();
        LocalDate targetDate = normalizeTargetDate(date);
        GymClassEntity gymClass = gymClassRepository.findById(classId)
            .orElseThrow(() -> new ResourceNotFoundException("Clase no encontrada: " + classId));
        WeekDay targetWeekDay = toWeekDay(targetDate);
        boolean ownClass = gymClass.getTeacher() != null && gymClass.getTeacher().getId().equals(user.getId());

        if (ownClass) {
            throw new BusinessValidationException("No podes suscribirte a una clase dictada por vos.");
        }

        if (!gymClass.getWeekDays().contains(targetWeekDay)) {
            throw new BusinessValidationException("La clase no se dicta el dia seleccionado.");
        }
        if (gymClassSubscriptionRepository.existsByGymClassIdAndUserIdAndTargetDate(classId, user.getId(), targetDate)) {
            throw new BusinessValidationException("Ya estas suscripto a esta clase.");
        }

        if (hasClassStarted(gymClass, targetDate)) {
            throw new BusinessValidationException("La clase ya comenzo.");
        }

        if (!isActivityAllowed(user, gymClass)) {
            throw new BusinessValidationException("Esta actividad no esta incluida en tus actividades habilitadas.");
        }

        validatePlanRestrictions(user, targetDate);

        long enrolledCount = gymClassSubscriptionRepository.countByGymClassIdAndTargetDate(classId, targetDate);
        if (gymClass.getMaxCapacity() != null && gymClass.getMaxCapacity() > 0 && enrolledCount >= gymClass.getMaxCapacity()) {
            throw new BusinessValidationException("La clase ya no tiene cupos disponibles.");
        }

        gymClassSubscriptionRepository.save(
            GymClassSubscriptionEntity.builder()
                .gymClass(gymClass)
                .user(user)
                .targetDate(targetDate)
                .subscribedAt(LocalDateTime.now())
                .build()
        );

        return toMobileClassDto(gymClass, user, targetDate, true);
    }

    private MobileRoutineResponseDto toRoutineDto(RoutineAssignmentEntity entity, LocalDate today) {
        String routineName = entity.getRoutineTemplate() == null
            ? (entity.getCustomName() == null || entity.getCustomName().isBlank() ? "Rutina Custom" : entity.getCustomName())
            : entity.getRoutineTemplate().getName();
        return MobileRoutineResponseDto.builder()
            .id(entity.getId())
            .routineTemplateName(routineName)
            .status(resolveRoutineStatus(entity, today))
            .days(entity.getDays())
            .startDate(entity.getStartDate())
            .endDate(entity.getEndDate())
            .exercises(entity.getExercisePlans().stream().map(this::toRoutineExerciseDto).toList())
            .build();
    }

    private MobileRoutineExerciseResponseDto toRoutineExerciseDto(RoutineAssignmentExerciseEntity item) {
        ExerciseEntity exercise = item.getExercise();
        return MobileRoutineExerciseResponseDto.builder()
            .exerciseId(exercise.getId())
            .exerciseName(item.getExerciseName())
            .activityName(exercise.getActivity() == null ? "Sin actividad" : exercise.getActivity().getName())
            .machineName(exercise.getMachine() == null ? "Peso libre" : exercise.getMachine().getName())
            .machinePhotoUrl(exercise.getMachine() == null ? null : exercise.getMachine().getPhotoUrl())
            .videoUrl(exercise.getVideoUrl())
            .sets(item.getSets())
            .prescriptionMode(item.getPrescriptionMode())
            .reps(item.getReps())
            .weight(item.getWeight())
            .minReps(item.getMinReps())
            .maxReps(item.getMaxReps())
            .minWeight(item.getMinWeight())
            .maxWeight(item.getMaxWeight())
            .build();
    }

    private String resolveRoutineStatus(RoutineAssignmentEntity entity, LocalDate today) {
        if (entity.getStartDate().isAfter(today)) {
            return "Proxima";
        }
        if (entity.getEndDate().isBefore(today)) {
            return "Finalizada";
        }
        return "Activa";
    }

    private int routinePriority(RoutineAssignmentEntity entity, LocalDate today) {
        if (!entity.getStartDate().isAfter(today) && !entity.getEndDate().isBefore(today)) {
            return 0;
        }
        if (entity.getStartDate().isAfter(today)) {
            return 1;
        }
        return 2;
    }

    private void ensureExerciseWorkedByUser(Long userId, Long exerciseId) {
        boolean exists = routineAssignmentRepository.findByUserIdOrderByStartDateDescIdDesc(userId).stream()
            .flatMap(item -> item.getExercisePlans().stream())
            .anyMatch(item -> item.getExercise().getId().equals(exerciseId));

        if (!exists) {
            throw new BusinessValidationException("Solo podes registrar pesos para ejercicios trabajados en tus rutinas.");
        }
    }

    private LocalDate normalizeTargetDate(LocalDate date) {
        LocalDate today = LocalDate.now();
        if (date == null || date.isBefore(today)) {
            return today;
        }
        return date;
    }

    private void validatePlanRestrictions(UserEntity user, LocalDate targetDate) {
        String restrictionReason = resolvePlanRestriction(user, targetDate);
        if (restrictionReason != null) {
            throw new BusinessValidationException(restrictionReason);
        }
    }

    private String resolveSubscriptionRestriction(
        GymClassEntity gymClass,
        UserEntity user,
        LocalDate targetDate,
        boolean subscribed,
        boolean ownClass,
        Integer availableSpots
    ) {
        if (subscribed) {
            return "Ya estas suscripto";
        }
        if (ownClass) {
            return "La dictas vos";
        }
        if (availableSpots != null && availableSpots == 0) {
            return "Sin cupos";
        }
        if (hasClassStarted(gymClass, targetDate)) {
            return "La clase ya comenzo";
        }
        if (!isActivityAllowed(user, gymClass)) {
            return "Esta actividad no esta incluida en tus actividades habilitadas.";
        }
        return resolvePlanRestriction(user, targetDate);
    }

    private String resolvePlanRestriction(UserEntity user, LocalDate targetDate) {
        if (user.getUserType() != com.fitexa.entities.UserType.ALUMNO || user.getPlan() == null) {
            return null;
        }
        if (hasExpiredOrOverduePlan(user, targetDate)) {
            return PLAN_PAYMENT_RESTRICTION_MESSAGE;
        }
        if (isFreePlan(user)) {
            return null;
        }

        List<GymClassSubscriptionEntity> sameDaySubscriptions = gymClassSubscriptionRepository.findByUserIdAndTargetDate(user.getId(), targetDate);
        if (!sameDaySubscriptions.isEmpty()) {
            return "Tu plan permite una sola actividad por dia.";
        }

        LocalDate weekStart = targetDate.minusDays(targetDate.getDayOfWeek().getValue() - 1L);
        LocalDate weekEnd = weekStart.plusDays(6);
        long weeklySubscriptions = gymClassSubscriptionRepository.findByUserIdAndTargetDateBetween(user.getId(), weekStart, weekEnd).size();
        if (user.getPlan().getMaxDaysPerWeek() != null && weeklySubscriptions >= user.getPlan().getMaxDaysPerWeek()) {
            return "Ya alcanzaste el limite semanal de tu plan.";
        }

        return null;
    }

    private boolean isFreePlan(UserEntity user) {
        if (user.getPlan() == null || user.getPlan().getName() == null) {
            return false;
        }
        return "libre".equalsIgnoreCase(user.getPlan().getName().trim());
    }

    private boolean hasExpiredOrOverduePlan(UserEntity user, LocalDate targetDate) {
        if (user.getPaymentStatus() != PaymentStatus.AL_DIA) {
            return true;
        }
        return user.getRenewalDate() != null && user.getRenewalDate().isBefore(targetDate);
    }

    private boolean hasClassStarted(GymClassEntity gymClass, LocalDate targetDate) {
        LocalDate today = LocalDate.now();
        if (!targetDate.isEqual(today)) {
            return false;
        }
        if (Boolean.TRUE.equals(gymClass.getAllDay())) {
            return true;
        }
        if (gymClass.getStartTime() == null) {
            return false;
        }
        return !gymClass.getStartTime().isAfter(java.time.LocalTime.now());
    }

    private boolean isActivityAllowed(UserEntity user, GymClassEntity gymClass) {
        if (user.getUserType() != com.fitexa.entities.UserType.ALUMNO) {
            return true;
        }
        if (gymClass.getActivity() == null) {
            return true;
        }
        return user.getActivities() != null
            && user.getActivities().stream().anyMatch(item -> item.getId().equals(gymClass.getActivity().getId()));
    }

    private MobileGymClassResponseDto toMobileClassDto(GymClassEntity item, UserEntity user, LocalDate targetDate, boolean subscribed) {
        int enrolled = Math.toIntExact(gymClassSubscriptionRepository.countByGymClassIdAndTargetDate(item.getId(), targetDate));
        Integer availableSpots = item.getMaxCapacity() == null ? null : Math.max(item.getMaxCapacity() - enrolled, 0);
        boolean ownClass = item.getTeacher() != null && item.getTeacher().getId().equals(user.getId());
        boolean activityAllowed = isActivityAllowed(user, item);
        String restrictionReason = resolveSubscriptionRestriction(item, user, targetDate, subscribed, ownClass, availableSpots);
        boolean canSubscribe = restrictionReason == null;

        return MobileGymClassResponseDto.builder()
            .id(item.getId())
            .className(item.getClassName())
            .activityName(item.getActivity() == null ? "Sin actividad" : item.getActivity().getName())
            .teacherName(item.getTeacher() == null ? "Staff Fitexa" : item.getTeacher().getFirstName() + " " + item.getTeacher().getLastName())
            .targetDate(targetDate)
            .allDay(Boolean.TRUE.equals(item.getAllDay()))
            .startTime(item.getStartTime())
            .endTime(item.getEndTime())
            .maxCapacity(item.getMaxCapacity())
            .enrolled(enrolled)
            .availableSpots(availableSpots)
            .subscribed(subscribed)
            .ownClass(ownClass)
            .activityAllowed(activityAllowed)
            .canSubscribe(canSubscribe)
            .restrictionReason(restrictionReason)
            .build();
    }

    private WeekDay toWeekDay(LocalDate date) {
        return switch (date.getDayOfWeek().getValue()) {
            case 1 -> WeekDay.LUNES;
            case 2 -> WeekDay.MARTES;
            case 3 -> WeekDay.MIERCOLES;
            case 4 -> WeekDay.JUEVES;
            case 5 -> WeekDay.VIERNES;
            case 6 -> WeekDay.SABADO;
            default -> WeekDay.DOMINGO;
        };
    }

    private MobilePaymentMovementResponseDto toMobilePaymentDto(UserPaymentMovementEntity entity) {
        return MobilePaymentMovementResponseDto.builder()
            .id(entity.getId())
            .planName(entity.getPlan() == null ? "Sin plan" : entity.getPlan().getName())
            .amount(entity.getAmount())
            .detail(entity.getDetail())
            .paidAt(entity.getPaidAt())
            .build();
    }

    private MobileProfileResponseDto toMobileProfileDto(UserEntity user) {
        return MobileProfileResponseDto.builder()
            .id(user.getId())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .email(user.getEmail())
            .phone(user.getPhone())
            .address(user.getAddress())
            .birthDate(user.getBirthDate())
            .userType(user.getUserType())
            .planName(user.getPlan() == null ? null : user.getPlan().getName())
            .planPrice(user.getPlan() == null ? null : user.getPlan().getPrice())
            .activityNames(
                user.getActivities() == null
                    ? List.of()
                    : user.getActivities().stream()
                        .map(item -> item.getName())
                        .sorted(String.CASE_INSENSITIVE_ORDER)
                        .toList()
            )
            .renewalDate(user.getRenewalDate())
            .paymentStatus(user.getPaymentStatus())
            .build();
    }
}
