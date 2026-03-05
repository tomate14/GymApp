package com.fitexa.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ActivityResponseDto {
    private Long id;
    private String name;
    private String description;
}
