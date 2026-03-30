package com.fitexa.dtos;

import com.fitexa.entities.AdminPanelModule;
import jakarta.validation.constraints.NotBlank;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Data;

@Data
public class RoleRequestDto {

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    private Set<AdminPanelModule> menuItems = new LinkedHashSet<>();
}
