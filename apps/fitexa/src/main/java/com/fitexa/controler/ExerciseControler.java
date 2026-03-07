package com.fitexa.controler;

import com.fitexa.dtos.ExerciseRequestDto;
import com.fitexa.dtos.ExerciseResponseDto;
import com.fitexa.dtos.DashboardPageResponseDto;
import com.fitexa.services.ExerciseService;
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
@RequestMapping("/api/v1/exercises")
@RequiredArgsConstructor
public class ExerciseControler {

    private final ExerciseService exerciseService;

    @GetMapping
    public List<ExerciseResponseDto> findAll() {
        return exerciseService.findAll();
    }

    @GetMapping("/page")
    public DashboardPageResponseDto<ExerciseResponseDto> findPage(
        @RequestParam(name = "name", required = false) String name,
        @RequestParam(name = "activityId", required = false) Long activityId,
        @RequestParam(name = "machineId", required = false) Long machineId,
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "8") int size
    ) {
        return exerciseService.findPage(name, activityId, machineId, page, size);
    }

    @GetMapping("/{id}")
    public ExerciseResponseDto findById(@PathVariable Long id) {
        return exerciseService.findById(id);
    }

    @PostMapping
    public ExerciseResponseDto create(@Valid @RequestBody ExerciseRequestDto request) {
        return exerciseService.create(request);
    }

    @PutMapping("/{id}")
    public ExerciseResponseDto update(@PathVariable Long id, @Valid @RequestBody ExerciseRequestDto request) {
        return exerciseService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        exerciseService.delete(id);
    }
}
