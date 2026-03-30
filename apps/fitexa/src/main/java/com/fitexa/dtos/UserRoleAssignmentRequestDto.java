package com.fitexa.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserRoleAssignmentRequestDto {

    @NotNull
    private Long roleId;
}
