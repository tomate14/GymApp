package com.fitexa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "client_subscription_payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientSubscriptionPaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_subscription_id", nullable = false)
    private ClientSubscriptionEntity subscription;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private ClientEntity client;

    @Column(length = 120)
    private String mercadoPagoAuthorizedPaymentId;

    @Column(length = 120)
    private String mercadoPagoPaymentId;

    @Column(nullable = false, length = 60)
    private String status;

    @Column(length = 60)
    private String statusDetail;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currencyId;

    private LocalDateTime paidAt;

    private LocalDateTime periodStart;

    private LocalDateTime periodEnd;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
