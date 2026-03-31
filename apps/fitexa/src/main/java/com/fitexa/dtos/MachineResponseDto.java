package com.fitexa.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MachineResponseDto {
    private Long id;
    private String name;
    private Integer quantity;
    private String photoUrl;
}
