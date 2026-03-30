package com.fitexa.dtos;

import com.fitexa.entities.AdminPanelModule;
import java.util.Set;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoleResponseDto {
    private Long id;
    private String name;
    private String description;
    private boolean systemRole;
    private long assignedUsersCount;
    private Set<AdminPanelModule> menuItems;
}
