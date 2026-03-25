package com.fitexa.controler;

import com.fitexa.dtos.ActivityRequestDto;
import com.fitexa.dtos.ActivityResponseDto;
import com.fitexa.dtos.DashboardPageResponseDto;
import com.fitexa.services.ActivityService;
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
@RequestMapping("/api/v1/activities")
@RequiredArgsConstructor
@Tag(name = "Actividades", description = "ABM de actividades del gimnasio.")
public class ActivityControler {

    private final ActivityService activityService;

    @GetMapping
    public List<ActivityResponseDto> findAll() {
        return activityService.findAll();
    }

    @GetMapping("/page")
    public DashboardPageResponseDto<ActivityResponseDto> findPage(
        @RequestParam(name = "name", required = false) String name,
        @RequestParam(name = "description", required = false) String description,
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "8") int size
    ) {
        return activityService.findPage(name, description, page, size);
    }

    @GetMapping("/{id}")
    public ActivityResponseDto findById(@PathVariable Long id) {
        return activityService.findById(id);
    }

    @PostMapping
    public ActivityResponseDto create(@Valid @RequestBody ActivityRequestDto request) {
        return activityService.create(request);
    }

    @PutMapping("/{id}")
    public ActivityResponseDto update(@PathVariable Long id, @Valid @RequestBody ActivityRequestDto request) {
        return activityService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        activityService.delete(id);
    }
}
