package com.fitexa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "billing_plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingPlanEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String code;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currencyId = "ARS";

    @Column(nullable = false)
    private Integer frequency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BillingFrequencyType frequencyType;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(length = 120)
    private String mercadoPagoPlanId;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
