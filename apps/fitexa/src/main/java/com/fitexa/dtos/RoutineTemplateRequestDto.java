package com.fitexa.dtos;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class RoutineTemplateRequestDto {
    @NotBlank
    private String name;

    private LocalDate createdAt;

    private List<Long> exerciseIds;
}
