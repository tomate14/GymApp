package com.fitexa.dtos.billing;

import com.fitexa.entities.BillingFrequencyType;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BillingPlanResponseDto {
    private Long id;
    private String code;
    private String name;
    private String description;
    private BigDecimal price;
    private String currencyId;
    private Integer frequency;
    private BillingFrequencyType frequencyType;
}
