package com.fitexa.services.impl;

import com.fitexa.dtos.RoleMenuItemResponseDto;
import com.fitexa.dtos.RoleRequestDto;
import com.fitexa.dtos.RoleResponseDto;
import com.fitexa.entities.AdminPanelModule;
import com.fitexa.entities.RoleEntity;
import com.fitexa.exeptions.BusinessValidationException;
import com.fitexa.exeptions.ResourceNotFoundException;
import com.fitexa.repository.RoleRepository;
import com.fitexa.repository.UserRepository;
import com.fitexa.services.RoleService;
import java.util.LinkedHashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponseDto> findAll() {
        return roleRepository.findAll().stream()
            .map(this::toDto)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponseDto findById(Long id) {
        return toDto(findEntity(id));
    }

    @Override
    @Transactional
    public RoleResponseDto create(RoleRequestDto request) {
        String normalizedName = normalizeName(request.getName());
        if (roleRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new BusinessValidationException("Ya existe un rol con ese nombre.");
        }

        RoleEntity entity = RoleEntity.builder()
            .name(normalizedName)
            .description(request.getDescription().trim())
            .systemRole(false)
            .menuItems(request.getMenuItems() == null ? new LinkedHashSet<>() : new LinkedHashSet<>(request.getMenuItems()))
            .build();

        return toDto(roleRepository.save(entity));
    }

    @Override
    @Transactional
    public RoleResponseDto update(Long id, RoleRequestDto request) {
        RoleEntity entity = findEntity(id);
        String normalizedName = normalizeName(request.getName());

        roleRepository.findByNameIgnoreCase(normalizedName)
            .filter(existing -> !existing.getId().equals(id))
            .ifPresent(existing -> {
                throw new BusinessValidationException("Ya existe un rol con ese nombre.");
            });

        entity.setName(normalizedName);
        entity.setDescription(request.getDescription().trim());
        entity.setMenuItems(request.getMenuItems() == null ? new LinkedHashSet<>() : new LinkedHashSet<>(request.getMenuItems()));

        return toDto(roleRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        RoleEntity entity = findEntity(id);
        if (entity.isSystemRole()) {
            throw new BusinessValidationException("No se puede eliminar un rol del sistema.");
        }
        if (userRepository.countByRoleId(id) > 0) {
            throw new BusinessValidationException("No se puede eliminar un rol que esta asignado a usuarios.");
        }
        roleRepository.delete(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleMenuItemResponseDto> findAvailableMenuItems() {
        return List.of(AdminPanelModule.values()).stream()
            .map(item -> RoleMenuItemResponseDto.builder()
                .key(item.getValue())
                .label(item.getLabel())
                .path(item.getPath())
                .build())
            .toList();
    }

    private RoleEntity findEntity(Long id) {
        return roleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + id));
    }

    private RoleResponseDto toDto(RoleEntity entity) {
        return RoleResponseDto.builder()
            .id(entity.getId())
            .name(entity.getName())
            .description(entity.getDescription())
            .systemRole(entity.isSystemRole())
            .assignedUsersCount(userRepository.countByRoleId(entity.getId()))
            .menuItems(new LinkedHashSet<>(entity.getMenuItems()))
            .build();
    }

    private String normalizeName(String value) {
        String normalized = value == null ? null : value.trim();
        if (normalized == null || normalized.isBlank()) {
            throw new BusinessValidationException("El nombre del rol es obligatorio.");
        }
        return normalized;
    }
}
