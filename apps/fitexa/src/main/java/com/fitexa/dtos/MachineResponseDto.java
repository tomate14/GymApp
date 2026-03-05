package com.fitexa.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MachineResponseDto {
    private Long id;
    private String name;
    private String photoUrl;
}
