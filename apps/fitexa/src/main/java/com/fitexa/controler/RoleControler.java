package com.fitexa.controler;

import com.fitexa.dtos.RoleMenuItemResponseDto;
import com.fitexa.dtos.RoleRequestDto;
import com.fitexa.dtos.RoleResponseDto;
import com.fitexa.services.RoleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Tag(name = "Roles", description = "Administracion de roles del panel y modulos visibles.")
public class RoleControler {

    private final RoleService roleService;

    @GetMapping
    public List<RoleResponseDto> findAll() {
        return roleService.findAll();
    }

    @GetMapping("/menu-items")
    public List<RoleMenuItemResponseDto> findMenuItems() {
        return roleService.findAvailableMenuItems();
    }

    @GetMapping("/{id}")
    public RoleResponseDto findById(@PathVariable Long id) {
        return roleService.findById(id);
    }

    @PostMapping
    public RoleResponseDto create(@Valid @RequestBody RoleRequestDto request) {
        return roleService.create(request);
    }

    @PutMapping("/{id}")
    public RoleResponseDto update(@PathVariable Long id, @Valid @RequestBody RoleRequestDto request) {
        return roleService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        roleService.delete(id);
    }
}
