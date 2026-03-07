package com.fitexa.services.impl;

import com.fitexa.dtos.PlanRequestDto;
import com.fitexa.dtos.PlanResponseDto;
import com.fitexa.dtos.DashboardPageResponseDto;
import com.fitexa.entities.PlanEntity;
import com.fitexa.exeptions.ResourceNotFoundException;
import com.fitexa.repository.PlanRepository;
import com.fitexa.services.PlanService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {

    private static final int MAX_PAGE_SIZE = 100;

    private final PlanRepository planRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PlanResponseDto> findAll() {
        return planRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardPageResponseDto<PlanResponseDto> findPage(String name, String status, int page, int size) {
        Page<PlanResponseDto> result = planRepository.findForTable(
            clean(name),
            parseStatus(status),
            PageRequest.of(sanitizePage(page), sanitizeSize(size), Sort.by(Sort.Direction.ASC, "name"))
        ).map(this::toDto);

        return toPageResponse(result);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanResponseDto> findActive() {
        return planRepository.findByActiveTrue().stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PlanResponseDto findById(Long id) {
        return toDto(findEntity(id));
    }

    @Override
    @Transactional
    public PlanResponseDto create(PlanRequestDto request) {
        PlanEntity entity = PlanEntity.builder()
            .name(request.getName())
            .price(request.getPrice())
            .maxDaysPerWeek(request.getMaxDaysPerWeek())
            .active(request.getActive())
            .build();
        return toDto(planRepository.save(entity));
    }

    @Override
    @Transactional
    public PlanResponseDto update(Long id, PlanRequestDto request) {
        PlanEntity entity = findEntity(id);
        entity.setName(request.getName());
        entity.setPrice(request.getPrice());
        entity.setMaxDaysPerWeek(request.getMaxDaysPerWeek());
        entity.setActive(request.getActive());
        return toDto(planRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        planRepository.delete(findEntity(id));
    }

    private PlanEntity findEntity(Long id) {
        return planRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Plan no encontrado: " + id));
    }

    private PlanResponseDto toDto(PlanEntity entity) {
        return PlanResponseDto.builder()
            .id(entity.getId())
            .name(entity.getName())
            .price(entity.getPrice())
            .maxDaysPerWeek(entity.getMaxDaysPerWeek())
            .active(entity.getActive())
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

    private String clean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private Boolean parseStatus(String status) {
        if (status == null || status.isBlank() || "all".equalsIgnoreCase(status)) {
            return null;
        }
        if ("active".equalsIgnoreCase(status)) {
            return true;
        }
        if ("inactive".equalsIgnoreCase(status)) {
            return false;
        }
        return null;
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
}
