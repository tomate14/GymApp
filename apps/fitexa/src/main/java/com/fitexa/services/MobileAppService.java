package com.fitexa.services;

import com.fitexa.dtos.mobile.MobileGymClassResponseDto;
import com.fitexa.dtos.mobile.MobilePaymentMovementResponseDto;
import com.fitexa.dtos.mobile.MobilePersonalRecordDetailResponseDto;
import com.fitexa.dtos.mobile.MobilePersonalRecordRequestDto;
import com.fitexa.dtos.mobile.MobilePersonalRecordResponseDto;
import com.fitexa.dtos.mobile.MobileProfileResponseDto;
import com.fitexa.dtos.mobile.MobileProfileUpdateRequestDto;
import com.fitexa.dtos.mobile.MobileRoutineResponseDto;
import java.time.LocalDate;
import java.util.List;

public interface MobileAppService {
    MobileProfileResponseDto getMyProfile();
    MobileProfileResponseDto updateMyProfile(MobileProfileUpdateRequestDto request);
    List<MobileRoutineResponseDto> getMyRoutines();
    List<MobilePaymentMovementResponseDto> getMyPaymentMovements();
    byte[] downloadMyPaymentMovementPdf(Long movementId);
    List<MobilePersonalRecordResponseDto> getMyPersonalRecords();
    MobilePersonalRecordDetailResponseDto getMyPersonalRecordDetail(Long exerciseId);
    MobilePersonalRecordResponseDto savePersonalRecord(Long exerciseId, MobilePersonalRecordRequestDto request);
    List<MobileGymClassResponseDto> getClassesForDate(LocalDate date);
    MobileGymClassResponseDto subscribeToClass(Long classId, LocalDate date);
}
