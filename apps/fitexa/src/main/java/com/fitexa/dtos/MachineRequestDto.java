package com.fitexa.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MachineRequestDto {
    @NotBlank
    private String name;

    @NotBlank
    private String photoUrl;
}
