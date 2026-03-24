package com.fitexa.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GymClassAttendanceUpdateRequestDto {
    @Valid
    @NotEmpty
    private List<GymClassAttendanceUpdateItemDto> students;
}
