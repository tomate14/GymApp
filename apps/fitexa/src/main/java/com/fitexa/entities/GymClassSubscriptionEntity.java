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
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "gym_class_subscriptions",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_gym_class_subscriptions_class_user_date",
            columnNames = {"gym_class_id", "user_id", "target_date"}
        )
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GymClassSubscriptionEntity extends ClientScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gym_class_id", nullable = false)
    private GymClassEntity gymClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private LocalDate targetDate;

    @Column(nullable = false)
    private LocalDateTime subscribedAt;

    @Column
    private Boolean attendancePresent;

    @Column
    private LocalDateTime attendanceMarkedAt;
}
