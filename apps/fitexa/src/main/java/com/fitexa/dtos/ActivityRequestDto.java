package com.fitexa.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ActivityRequestDto {
    @NotBlank
    private String name;

    @NotBlank
    private String description;
}
