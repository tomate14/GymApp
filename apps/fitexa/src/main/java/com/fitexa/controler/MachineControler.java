package com.fitexa.controler;

import com.fitexa.dtos.MachineRequestDto;
import com.fitexa.dtos.MachineResponseDto;
import com.fitexa.dtos.DashboardPageResponseDto;
import com.fitexa.services.MachineService;
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
@RequestMapping("/api/v1/machines")
@RequiredArgsConstructor
public class MachineControler {

    private final MachineService machineService;

    @GetMapping
    public List<MachineResponseDto> findAll() {
        return machineService.findAll();
    }

    @GetMapping("/page")
    public DashboardPageResponseDto<MachineResponseDto> findPage(
        @RequestParam(name = "name", required = false) String name,
        @RequestParam(name = "onlyInUse", defaultValue = "false") boolean onlyInUse,
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "8") int size
    ) {
        return machineService.findPage(name, onlyInUse, page, size);
    }

    @GetMapping("/{id}")
    public MachineResponseDto findById(@PathVariable Long id) {
        return machineService.findById(id);
    }

    @PostMapping
    public MachineResponseDto create(@Valid @RequestBody MachineRequestDto request) {
        return machineService.create(request);
    }

    @PutMapping("/{id}")
    public MachineResponseDto update(@PathVariable Long id, @Valid @RequestBody MachineRequestDto request) {
        return machineService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        machineService.delete(id);
    }
}
