package com.fitexa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Entity;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "gym_classes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GymClassEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String className;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "activity_id")
    private ActivityEntity activity;

    private Integer maxCapacity;

    @Column(nullable = false)
    private Integer enrolled;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "teacher_id")
    private UserEntity teacher;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "all_day", nullable = false)
    @Builder.Default
    private Boolean allDay = false;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "gym_class_days", joinColumns = @JoinColumn(name = "gym_class_id"))
    @Column(name = "day", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<WeekDay> weekDays = new LinkedHashSet<>();
}
