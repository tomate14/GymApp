package com.fitexa.controler;

import com.fitexa.dtos.mobile.MobileGymClassResponseDto;
import com.fitexa.dtos.mobile.MobilePaymentMovementResponseDto;
import com.fitexa.dtos.mobile.MobilePersonalRecordDetailResponseDto;
import com.fitexa.dtos.mobile.MobilePersonalRecordRequestDto;
import com.fitexa.dtos.mobile.MobilePersonalRecordResponseDto;
import com.fitexa.dtos.mobile.MobileProfileResponseDto;
import com.fitexa.dtos.mobile.MobileProfileUpdateRequestDto;
import com.fitexa.dtos.mobile.MobileRoutineResponseDto;
import com.fitexa.services.MobileAppService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/mobile")
@RequiredArgsConstructor
public class MobileAppControler {

    private final MobileAppService mobileAppService;

    @GetMapping("/me")
    public MobileProfileResponseDto getMyProfile() {
        return mobileAppService.getMyProfile();
    }

    @PutMapping("/me")
    public MobileProfileResponseDto updateMyProfile(@Valid @RequestBody MobileProfileUpdateRequestDto request) {
        return mobileAppService.updateMyProfile(request);
    }

    @GetMapping("/payments")
    public List<MobilePaymentMovementResponseDto> getMyPaymentMovements() {
        return mobileAppService.getMyPaymentMovements();
    }

    @GetMapping(value = "/payments/{movementId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadMyPaymentMovementPdf(@PathVariable Long movementId) {
        byte[] pdfBytes = mobileAppService.downloadMyPaymentMovementPdf(movementId);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"comprobante-pago-" + movementId + ".pdf\"")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdfBytes);
    }

    @GetMapping("/routines")
    public List<MobileRoutineResponseDto> getMyRoutines() {
        return mobileAppService.getMyRoutines();
    }

    @GetMapping("/personal-records")
    public List<MobilePersonalRecordResponseDto> getMyPersonalRecords() {
        return mobileAppService.getMyPersonalRecords();
    }

    @GetMapping("/personal-records/{exerciseId}")
    public MobilePersonalRecordDetailResponseDto getMyPersonalRecordDetail(@PathVariable Long exerciseId) {
        return mobileAppService.getMyPersonalRecordDetail(exerciseId);
    }

    @PutMapping("/personal-records/{exerciseId}")
    public MobilePersonalRecordResponseDto savePersonalRecord(
        @PathVariable Long exerciseId,
        @Valid @RequestBody MobilePersonalRecordRequestDto request
    ) {
        return mobileAppService.savePersonalRecord(exerciseId, request);
    }

    @GetMapping("/classes")
    public List<MobileGymClassResponseDto> getClassesForDate(
        @RequestParam(name = "date", required = false) LocalDate date
    ) {
        return mobileAppService.getClassesForDate(date);
    }

    @PostMapping("/classes/{classId}/subscribe")
    public MobileGymClassResponseDto subscribeToClass(
        @PathVariable Long classId,
        @RequestParam(name = "date", required = false) LocalDate date
    ) {
        return mobileAppService.subscribeToClass(classId, date);
    }
}
