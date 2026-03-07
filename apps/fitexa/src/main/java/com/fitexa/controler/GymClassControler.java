package com.fitexa.controler;

import com.fitexa.dtos.GymClassRequestDto;
import com.fitexa.dtos.GymClassResponseDto;
import com.fitexa.dtos.DashboardPageResponseDto;
import com.fitexa.services.GymClassService;
import jakarta.validation.Valid;
import java.time.LocalDate;
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
@RequestMapping("/api/v1/classes")
@RequiredArgsConstructor
public class GymClassControler {

    private final GymClassService gymClassService;

    @GetMapping
    public List<GymClassResponseDto> findAll() {
        return gymClassService.findAll();
    }

    @GetMapping("/page")
    public DashboardPageResponseDto<GymClassResponseDto> findPage(
        @RequestParam(name = "className", required = false) String className,
        @RequestParam(name = "activityId", required = false) Long activityId,
        @RequestParam(name = "teacherId", required = false) Long teacherId,
        @RequestParam(name = "dateFrom", required = false) LocalDate dateFrom,
        @RequestParam(name = "dateTo", required = false) LocalDate dateTo,
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "8") int size
    ) {
        return gymClassService.findPage(className, activityId, teacherId, dateFrom, dateTo, page, size);
    }

    @GetMapping("/{id}")
    public GymClassResponseDto findById(@PathVariable Long id) {
        return gymClassService.findById(id);
    }

    @PostMapping
    public GymClassResponseDto create(@Valid @RequestBody GymClassRequestDto request) {
        return gymClassService.create(request);
    }

    @PutMapping("/{id}")
    public GymClassResponseDto update(@PathVariable Long id, @Valid @RequestBody GymClassRequestDto request) {
        return gymClassService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        gymClassService.delete(id);
    }
}
