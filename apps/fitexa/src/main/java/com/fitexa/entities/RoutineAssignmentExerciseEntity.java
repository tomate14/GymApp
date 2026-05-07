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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "routine_assignment_exercises")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutineAssignmentExerciseEntity extends ClientScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private RoutineAssignmentEntity assignment;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "exercise_id", nullable = false)
    private ExerciseEntity exercise;

    @Column(nullable = false)
    private String exerciseName;

    @Column(nullable = false)
    private Integer sets;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrescriptionMode prescriptionMode;

    private Integer reps;
    private Double weight;
    private Integer minReps;
    private Integer maxReps;
    private Double minWeight;
    private Double maxWeight;
}
