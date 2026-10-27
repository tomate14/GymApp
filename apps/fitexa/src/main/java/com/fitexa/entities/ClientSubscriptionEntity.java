package com.fitexa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "client_subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientSubscriptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private ClientEntity client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "billing_plan_id", nullable = false)
    private BillingPlanEntity billingPlan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    @Builder.Default
    private ClientSubscriptionStatus status = ClientSubscriptionStatus.PENDING;

    @Column(length = 120)
    private String mercadoPagoPreapprovalId;

    @Column(length = 120)
    private String mercadoPagoPlanId;

    @Column(length = 220)
    private String payerEmail;

    @Column(length = 1000)
    private String checkoutUrl;

    private LocalDateTime nextPaymentAt;

    private LocalDateTime currentPeriodStart;

    private LocalDateTime currentPeriodEnd;

    private LocalDateTime activatedAt;

    private LocalDateTime cancelledAt;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    public boolean isActive() {
        return currentPeriodEnd != null &&
            !currentPeriodEnd.isBefore(LocalDateTime.now()) &&
            status != ClientSubscriptionStatus.PAYMENT_REQUIRED &&
            status != ClientSubscriptionStatus.EXPIRED;
    }
}
