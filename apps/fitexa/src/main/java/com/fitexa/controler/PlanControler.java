package com.fitexa.controler;

import com.fitexa.dtos.PlanRequestDto;
import com.fitexa.dtos.PlanResponseDto;
import com.fitexa.dtos.DashboardPageResponseDto;
import com.fitexa.services.PlanService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/plans")
@RequiredArgsConstructor
@Tag(name = "Planes", description = "ABM de planes comerciales del gimnasio.")
public class PlanControler {

    private final PlanService planService;

    @GetMapping
    public List<PlanResponseDto> findAll(@RequestParam(name = "activeOnly", defaultValue = "false") boolean activeOnly) {
        return activeOnly ? planService.findActive() : planService.findAll();
    }

    @GetMapping("/page")
    public DashboardPageResponseDto<PlanResponseDto> findPage(
        @RequestParam(name = "name", required = false) String name,
        @RequestParam(name = "status", required = false) String status,
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "8") int size
    ) {
        return planService.findPage(name, status, page, size);
    }

    @GetMapping("/{id}")
    public PlanResponseDto findById(@PathVariable Long id) {
        return planService.findById(id);
    }

    @PostMapping
    public PlanResponseDto create(@Valid @RequestBody PlanRequestDto request) {
        return planService.create(request);
    }

    @PutMapping("/{id}")
    public PlanResponseDto update(@PathVariable Long id, @Valid @RequestBody PlanRequestDto request) {
        return planService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        planService.delete(id);
    }
}
