package com.fitexa.repository;

import com.fitexa.entities.ExercisePersonalRecordEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExercisePersonalRecordRepository extends JpaRepository<ExercisePersonalRecordEntity, Long> {
    List<ExercisePersonalRecordEntity> findByUserIdAndClientId(Long userId, Long clientId);
    Optional<ExercisePersonalRecordEntity> findByUserIdAndExerciseIdAndClientId(Long userId, Long exerciseId, Long clientId);
}
