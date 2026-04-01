package com.fitexa.dtos.mobile;

import com.fitexa.entities.PaymentStatus;
import com.fitexa.entities.UserType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MobileProfileResponseDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private LocalDate birthDate;
    private UserType userType;
    private String planName;
    private BigDecimal planPrice;
    private List<String> activityNames;
    private LocalDate renewalDate;
    private PaymentStatus paymentStatus;
}
