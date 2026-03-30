package com.fitexa.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoleMenuItemResponseDto {
    private String key;
    private String label;
    private String path;
}
