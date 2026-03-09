package com.fitexa.controler;

import com.fitexa.dtos.MachineResponseDto;
import com.fitexa.dtos.DashboardPageResponseDto;
import com.fitexa.services.MachineService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MachineResponseDto create(
        @RequestParam("name") String name,
        @RequestPart("photo") MultipartFile photo
    ) {
        return machineService.create(name, photo);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MachineResponseDto update(
        @PathVariable Long id,
        @RequestParam("name") String name,
        @RequestPart(value = "photo", required = false) MultipartFile photo
    ) {
        return machineService.update(id, name, photo);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        machineService.delete(id);
    }
}
