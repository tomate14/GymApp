package com.fitexa.controler;

import com.fitexa.dtos.ActivityRequestDto;
import com.fitexa.dtos.ActivityResponseDto;
import com.fitexa.services.ActivityService;
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
@RequestMapping("/api/v1/activities")
@RequiredArgsConstructor
public class ActivityControler {

    private final ActivityService activityService;

    @GetMapping
    public List<ActivityResponseDto> findAll() {
        return activityService.findAll();
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
