package com.fitexa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mercado_pago_accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MercadoPagoAccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String name;

    @Column(length = 2000)
    private String encryptedAccessToken;

    @Column(length = 1000)
    private String encryptedPublicKey;

    @Column(length = 1000)
    private String encryptedWebhookSecret;

    @Column(nullable = false, length = 500)
    private String publicBaseUrl;

    @Column(nullable = false, length = 500)
    private String appBaseUrl;

    @Column(nullable = false)
    @Builder.Default
    private Boolean sandboxMode = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
