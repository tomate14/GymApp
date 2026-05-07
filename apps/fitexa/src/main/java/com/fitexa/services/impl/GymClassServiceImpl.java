package com.fitexa.services.impl;

import com.fitexa.dtos.GymClassAttendanceResponseDto;
import com.fitexa.dtos.GymClassAttendanceStudentDto;
import com.fitexa.dtos.GymClassAttendanceUpdateItemDto;
import com.fitexa.dtos.GymClassAttendanceUpdateRequestDto;
import com.fitexa.dtos.GymClassRequestDto;
import com.fitexa.dtos.GymClassResponseDto;
import com.fitexa.dtos.DashboardPageResponseDto;
import com.fitexa.entities.ActivityEntity;
import com.fitexa.entities.GymClassEntity;
import com.fitexa.entities.GymClassSubscriptionEntity;
import com.fitexa.entities.UserEntity;
import com.fitexa.entities.UserType;
import com.fitexa.entities.WeekDay;
import com.fitexa.exeptions.BusinessValidationException;
import com.fitexa.exeptions.ResourceNotFoundException;
import com.fitexa.repository.ActivityRepository;
import com.fitexa.repository.GymClassRepository;
import com.fitexa.repository.GymClassSubscriptionRepository;
import com.fitexa.repository.UserRepository;
import com.fitexa.services.AuthenticatedUserService;
import com.fitexa.services.GymClassService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GymClassServiceImpl implements GymClassService {

    private static final int MAX_PAGE_SIZE = 100;

    private final GymClassRepository gymClassRepository;
    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final GymClassSubscriptionRepository gymClassSubscriptionRepository;
    private final AuthenticatedUserService authenticatedUserService;

    @Override
    @Transactional(readOnly = true)
    public List<GymClassResponseDto> findAll() {
        return gymClassRepository.findAllByClientId(currentClientId()).stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardPageResponseDto<GymClassResponseDto> findPage(
        String className,
        Long activityId,
        Long teacherId,
        String weekDay,
        int page,
        int size
    ) {
        WeekDay parsedWeekDay = parseWeekDay(weekDay);
        Page<GymClassResponseDto> result = gymClassRepository.findForTable(
            currentClientId(),
            clean(className),
            activityId,
            teacherId,
            parsedWeekDay,
            PageRequest.of(
                sanitizePage(page),
                sanitizeSize(size),
                Sort.by(Sort.Direction.ASC, "startTime")
            )
        ).map(this::toDto);

        return toPageResponse(result);
    }

    @Override
    @Transactional(readOnly = true)
    public GymClassResponseDto findById(Long id) {
        return toDto(findEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public GymClassAttendanceResponseDto getAttendance(Long id, LocalDate targetDate) {
        GymClassEntity gymClass = findEntity(id);
        LocalDate effectiveDate = targetDate == null ? LocalDate.now() : targetDate;
        List<GymClassAttendanceStudentDto> students = gymClassSubscriptionRepository
            .findDetailedByGymClassIdAndTargetDate(id, currentClientId(), effectiveDate)
            .stream()
            .map(this::toAttendanceStudentDto)
            .toList();

        return GymClassAttendanceResponseDto.builder()
            .classId(gymClass.getId())
            .className(gymClass.getClassName())
            .targetDate(effectiveDate)
            .students(students)
            .build();
    }

    @Override
    @Transactional
    public GymClassAttendanceResponseDto updateAttendance(Long id, LocalDate targetDate, GymClassAttendanceUpdateRequestDto request) {
        GymClassEntity gymClass = findEntity(id);
        LocalDate effectiveDate = targetDate == null ? LocalDate.now() : targetDate;
        List<GymClassSubscriptionEntity> subscriptions = gymClassSubscriptionRepository.findDetailedByGymClassIdAndTargetDate(id, currentClientId(), effectiveDate);
        Map<Long, GymClassSubscriptionEntity> subscriptionsById = new LinkedHashMap<>();
        for (GymClassSubscriptionEntity subscription : subscriptions) {
            subscriptionsById.put(subscription.getId(), subscription);
        }

        LocalDateTime now = LocalDateTime.now();
        for (GymClassAttendanceUpdateItemDto item : request.getStudents()) {
            GymClassSubscriptionEntity subscription = subscriptionsById.get(item.getSubscriptionId());
            if (subscription == null) {
                throw new BusinessValidationException("La asistencia incluye alumnos que no pertenecen a esta clase o fecha.");
            }
            subscription.setAttendancePresent(item.getAttendancePresent());
            subscription.setAttendanceMarkedAt(now);
        }

        gymClassSubscriptionRepository.saveAll(subscriptions);

        return GymClassAttendanceResponseDto.builder()
            .classId(gymClass.getId())
            .className(gymClass.getClassName())
            .targetDate(effectiveDate)
            .students(subscriptions.stream().map(this::toAttendanceStudentDto).toList())
            .build();
    }

    @Override
    @Transactional
    public GymClassResponseDto create(GymClassRequestDto request) {
        boolean allDay = Boolean.TRUE.equals(request.getAllDay());
        validateSchedule(allDay, request.getStartTime(), request.getEndTime());
        Set<WeekDay> weekDays = sanitizeWeekDays(request.getWeekDays());
        GymClassEntity entity = GymClassEntity.builder()
            .className(request.getClassName())
            .activity(findActivity(request.getActivityId()))
            .maxCapacity(request.getMaxCapacity())
            .enrolled(0)
            .teacher(findTeacher(request.getTeacherId()))
            .weekDays(weekDays)
            .date(resolveAnchorDate(weekDays))
            .allDay(allDay)
            .startTime(allDay ? null : request.getStartTime())
            .endTime(allDay ? null : request.getEndTime())
            .build();
        entity.setClient(authenticatedUserService.getCurrentClient());
        return toDto(gymClassRepository.save(entity));
    }

    @Override
    @Transactional
    public GymClassResponseDto update(Long id, GymClassRequestDto request) {
        boolean allDay = Boolean.TRUE.equals(request.getAllDay());
        validateSchedule(allDay, request.getStartTime(), request.getEndTime());
        Set<WeekDay> weekDays = sanitizeWeekDays(request.getWeekDays());
        GymClassEntity entity = findEntity(id);
        entity.setClassName(request.getClassName());
        entity.setActivity(findActivity(request.getActivityId()));
        entity.setMaxCapacity(request.getMaxCapacity());
        entity.setTeacher(findTeacher(request.getTeacherId()));
        entity.setWeekDays(weekDays);
        entity.setDate(resolveAnchorDate(weekDays));
        entity.setAllDay(allDay);
        entity.setStartTime(allDay ? null : request.getStartTime());
        entity.setEndTime(allDay ? null : request.getEndTime());
        entity.setEnrolled(resolveEnrolledCount(entity));
        return toDto(gymClassRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        gymClassRepository.delete(findEntity(id));
    }

    private GymClassEntity findEntity(Long id) {
        return gymClassRepository.findByIdAndClientId(id, currentClientId())
            .orElseThrow(() -> new ResourceNotFoundException("Clase no encontrada: " + id));
    }

    private ActivityEntity findActivity(Long id) {
        if (id == null) {
            return null;
        }
        return activityRepository.findByIdAndClientId(id, currentClientId())
            .orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada: " + id));
    }

    private UserEntity findTeacher(Long id) {
        if (id == null) {
            return null;
        }
        UserEntity teacher = userRepository.findByIdAndClientId(id, currentClientId())
            .orElseThrow(() -> new ResourceNotFoundException("Profesor no encontrado: " + id));
        if (teacher.getUserType() != UserType.PROFESOR) {
            throw new BusinessValidationException("El usuario seleccionado no es profesor.");
        }
        return teacher;
    }

    private GymClassResponseDto toDto(GymClassEntity entity) {
        int enrolled = resolveEnrolledCount(entity);

        return GymClassResponseDto.builder()
            .id(entity.getId())
            .className(entity.getClassName())
            .activityId(entity.getActivity() == null ? null : entity.getActivity().getId())
            .activityName(entity.getActivity() == null ? null : entity.getActivity().getName())
            .maxCapacity(entity.getMaxCapacity())
            .enrolled(enrolled)
            .teacherId(entity.getTeacher() == null ? null : entity.getTeacher().getId())
            .teacherName(entity.getTeacher() == null ? null : entity.getTeacher().getFirstName() + " " + entity.getTeacher().getLastName())
            .date(entity.getDate())
            .allDay(Boolean.TRUE.equals(entity.getAllDay()))
            .startTime(entity.getStartTime())
            .endTime(entity.getEndTime())
            .weekDays(entity.getWeekDays() == null ? new LinkedHashSet<>() : new LinkedHashSet<>(entity.getWeekDays()))
            .build();
    }

    private GymClassAttendanceStudentDto toAttendanceStudentDto(GymClassSubscriptionEntity entity) {
        UserEntity user = entity.getUser();
        return GymClassAttendanceStudentDto.builder()
            .subscriptionId(entity.getId())
            .studentId(user.getId())
            .studentName(user.getFirstName() + " " + user.getLastName())
            .dni(user.getDni())
            .phone(user.getPhone())
            .attendancePresent(entity.getAttendancePresent())
            .attendanceMarkedAt(entity.getAttendanceMarkedAt())
            .build();
    }

    private int resolveEnrolledCount(GymClassEntity entity) {
        return entity.getEnrolled() == null ? 0 : entity.getEnrolled();
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

    private WeekDay parseWeekDay(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return WeekDay.fromValue(value.trim());
        } catch (IllegalArgumentException ex) {
            throw new BusinessValidationException(ex.getMessage());
        }
    }

    private Set<WeekDay> sanitizeWeekDays(Set<WeekDay> input) {
        if (input == null || input.isEmpty()) {
            throw new BusinessValidationException("Debe seleccionar al menos un dia para la clase.");
        }
        return new LinkedHashSet<>(input);
    }

    private void validateSchedule(boolean allDay, LocalTime startTime, LocalTime endTime) {
        if (allDay) {
            return;
        }
        if (startTime == null || endTime == null) {
            throw new BusinessValidationException("Debe indicar hora de inicio y hora de finalizacion o marcar la clase como todo el dia.");
        }
        if (!startTime.isBefore(endTime)) {
            throw new BusinessValidationException("La hora de fin debe ser mayor que la hora de inicio.");
        }
    }

    private LocalDate resolveAnchorDate(Set<WeekDay> days) {
        LocalDate today = LocalDate.now();
        for (int offset = 0; offset < 7; offset++) {
            LocalDate candidate = today.plusDays(offset);
            WeekDay candidateDay = toWeekDay(candidate.getDayOfWeek().getValue());
            if (days.contains(candidateDay)) {
                return candidate;
            }
        }
        return today;
    }

    private WeekDay toWeekDay(int dayOfWeekValue) {
        return switch (dayOfWeekValue) {
            case 1 -> WeekDay.LUNES;
            case 2 -> WeekDay.MARTES;
            case 3 -> WeekDay.MIERCOLES;
            case 4 -> WeekDay.JUEVES;
            case 5 -> WeekDay.VIERNES;
            case 6 -> WeekDay.SABADO;
            default -> WeekDay.DOMINGO;
        };
    }

    private Long currentClientId() {
        return authenticatedUserService.getCurrentClientId();
    }
}
