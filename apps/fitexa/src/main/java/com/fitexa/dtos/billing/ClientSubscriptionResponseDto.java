package com.fitexa.dtos.billing;

import com.fitexa.entities.ClientSubscriptionStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClientSubscriptionResponseDto {
    private Long clientId;
    private String clientName;
    private ClientSubscriptionStatus status;
    private boolean active;
    private Long billingPlanId;
    private String billingPlanName;
    private BigDecimal price;
    private String currencyId;
    private LocalDateTime nextPaymentAt;
    private LocalDateTime currentPeriodStart;
    private LocalDateTime currentPeriodEnd;
    private LocalDateTime activatedAt;
    private LocalDateTime cancelledAt;
    private String checkoutUrl;
}
