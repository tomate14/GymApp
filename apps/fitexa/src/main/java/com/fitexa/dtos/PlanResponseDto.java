package com.fitexa.dtos;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlanResponseDto {
    private Long id;
    private String name;
    private BigDecimal price;
    private Integer maxDaysPerWeek;
    private Boolean active;
}
