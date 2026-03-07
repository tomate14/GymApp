package com.fitexa.controler;

import com.fitexa.dtos.RoutineAssignmentRequestDto;
import com.fitexa.dtos.RoutineAssignmentResponseDto;
import com.fitexa.dtos.RoutineAssignmentSaveResultDto;
import com.fitexa.dtos.DashboardPageResponseDto;
import com.fitexa.services.RoutineAssignmentPdfService;
import com.fitexa.services.RoutineAssignmentService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/v1/routine-assignments")
@RequiredArgsConstructor
public class RoutineAssignmentControler {

    private final RoutineAssignmentService routineAssignmentService;
    private final RoutineAssignmentPdfService routineAssignmentPdfService;

    @GetMapping
    public List<RoutineAssignmentResponseDto> findAll() {
        return routineAssignmentService.findAll();
    }

    @GetMapping("/page")
    public DashboardPageResponseDto<RoutineAssignmentResponseDto> findPage(
        @RequestParam(name = "student", required = false) String student,
        @RequestParam(name = "templateId", required = false) Long templateId,
        @RequestParam(name = "currentOnly", defaultValue = "true") boolean currentOnly,
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "8") int size
    ) {
        return routineAssignmentService.findPage(student, templateId, currentOnly, page, size);
    }

    @GetMapping("/{id}")
    public RoutineAssignmentResponseDto findById(@PathVariable Long id) {
        return routineAssignmentService.findById(id);
    }

    @PostMapping
    public RoutineAssignmentSaveResultDto create(@Valid @RequestBody RoutineAssignmentRequestDto request) {
        return routineAssignmentService.create(request);
    }

    @PutMapping("/{id}")
    public RoutineAssignmentSaveResultDto update(@PathVariable Long id, @Valid @RequestBody RoutineAssignmentRequestDto request) {
        return routineAssignmentService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        routineAssignmentService.delete(id);
    }

    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        byte[] pdfBytes = routineAssignmentPdfService.generatePdf(id);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"rutina-asignacion-" + id + ".pdf\"")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdfBytes);
    }
}
