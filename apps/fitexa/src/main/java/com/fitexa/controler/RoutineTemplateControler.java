package com.fitexa.controler;

import com.fitexa.dtos.RoutineTemplateRequestDto;
import com.fitexa.dtos.RoutineTemplateResponseDto;
import com.fitexa.services.RoutineTemplateService;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/routine-templates")
@RequiredArgsConstructor
public class RoutineTemplateControler {

    private final RoutineTemplateService routineTemplateService;

    @GetMapping
    public List<RoutineTemplateResponseDto> findAll() {
        return routineTemplateService.findAll();
    }

    @GetMapping("/{id}")
    public RoutineTemplateResponseDto findById(@PathVariable Long id) {
        return routineTemplateService.findById(id);
    }

    @PostMapping
    public RoutineTemplateResponseDto create(@Valid @RequestBody RoutineTemplateRequestDto request) {
        return routineTemplateService.create(request);
    }

    @PutMapping("/{id}")
    public RoutineTemplateResponseDto update(@PathVariable Long id, @Valid @RequestBody RoutineTemplateRequestDto request) {
        return routineTemplateService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        routineTemplateService.delete(id);
    }
}
