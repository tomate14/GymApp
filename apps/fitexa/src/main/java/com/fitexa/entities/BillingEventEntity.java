package com.fitexa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "billing_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 220)
    private String eventKey;

    @Column(length = 80)
    private String provider;

    @Column(length = 80)
    private String eventType;

    @Column(length = 120)
    private String providerResourceId;

    @Column
    private Long clientId;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private ClientSubscriptionStatus resultingStatus;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime receivedAt = LocalDateTime.now();
}
