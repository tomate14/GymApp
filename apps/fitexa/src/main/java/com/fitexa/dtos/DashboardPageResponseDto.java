package com.fitexa.dtos;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardPageResponseDto<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
