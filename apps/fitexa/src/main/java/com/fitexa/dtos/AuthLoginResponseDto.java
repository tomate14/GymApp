package com.fitexa.dtos;

import com.fitexa.entities.AdminPanelModule;
import java.util.Set;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthLoginResponseDto {
    private String token;
    private String tokenType;
    private long expiresInSeconds;
    private Long userId;
    private Long clientId;
    private String clientName;
    private String clientSlug;
    private String fullName;
    private String email;
    private String userType;
    private Long roleId;
    private String roleName;
    private Set<AdminPanelModule> menuItems;
    private boolean mustChangePassword;
}
